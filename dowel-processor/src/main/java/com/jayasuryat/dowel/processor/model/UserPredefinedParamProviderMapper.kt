/*
 * Copyright 2022 Jaya Surya Thotapalli
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jayasuryat.dowel.processor.model

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.dowelClassName
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviderMapper.ProcessedConsiderForDowelSymbols
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.jayasuryat.either.Either
import com.jayasuryat.either.left
import com.jayasuryat.either.right

/**
 * Mapper class to map [List]&lt;[KSAnnotated]&gt; to [ProcessedConsiderForDowelSymbols].
 *
 * This mapper assumes that all of the [KSAnnotated] symbols are annotated with [ConsiderForDowel]
 * annotation. And expects all of the [KSAnnotated] to be instances of [KSClassDeclaration]
 * type and all of them to be extending [androidx.compose.ui.tooling.preview.PreviewParameterProvider],
 * and there should only be a single [KSAnnotated] per [KSType].
 *
 * **Note** : This mapper will call [KSTypeReference.resolve] method on super types of every mapped
 * [KSClassDeclaration], which is an expensive call. It is suggested to map a [List]&lt;[KSAnnotated]&gt
 * once and reuse the resultant [UserPredefinedParamProviders] instead of mapping multiple times.
 *
 * @param resolver to resolve super types of each [KSClassDeclaration]
 * @param logger to log errors when unexpected or invalid inputs are encountered
 *
 * @see [UserPredefinedParamProviders]
 * @see [ProcessedConsiderForDowelSymbols]
 * @see [KSTypeReference.resolve]
 */
@Suppress("KDocUnresolvedReference")
internal class UserPredefinedParamProviderMapper(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {

    private val previewParamProviderType: KSType by unsafeLazy {
        val name = resolver.getKSNameFromString(name = Names.previewParamProvider.canonicalName)
        resolver.getClassDeclarationByName(name)!!.asStarProjectedType()
    }

    fun map(
        declarations: List<KSAnnotated>,
    ): ProcessedConsiderForDowelSymbols {

        val validSymbols: MutableList<KSClassDeclaration> = mutableListOf()
        val invalidSymbols: MutableList<KSAnnotated> = mutableListOf()

        declarations.forEach { symbol ->
            when (val validated = symbol.validateAndMap()) {
                is Either.Left -> invalidSymbols.add(validated.value)
                is Either.Right -> validSymbols.add(validated.value)
            }
        }

        val mapped: ProcessedConsiderForDowelSymbols = validSymbols.validateAndMap()

        // Adding unprocessed invalid symbols to the invalidSymbols list
        return mapped.copy(
            invalidSymbols = mapped.invalidSymbols + invalidSymbols,
        )
    }

    /**
     * Logs warning when overlapping providers are found for a [KSType]. Overlapping providers
     * could be coming from different sources. For example, a class could be annotated with
     * @[Dowel] annotation, and a user defined PreviewParameterProvider implementation exists
     * for the same class which is annotated with @[ConsiderForDowel] annotation. At this point
     * Dowel generates a provider for that type and a pre-defined provider already exists for the
     * same type, which is redundant.
     */
    fun logWarningForOverlappingDowelClasses(
        predefinedProviders: UserPredefinedParamProviders,
        dowelDeclarations: List<KSClassDeclaration>,
    ) {

        val declarations: Map<KSClassDeclaration, KSType> = dowelDeclarations
            .associateWith { declaration -> declaration.asType(listOf()) }

        declarations.forEach { (declaration: KSClassDeclaration, type: KSType) ->

            val existingProvider: KSClassDeclaration? = predefinedProviders[type]

            if (existingProvider != null) {

                val existingName = existingProvider.simpleName.asString()
                val generatedName = declaration.dowelClassName

                logger.warn(
                    "Duplicate/redundant providers found for type : $type, $existingName and $generatedName.\n" +
                        "$existingName will take precedence and will be used in outputs. $generatedName will be ignored.\n" +
                        "Consider either removing the redundant @${Dowel::class.simpleName!!} annotation from the ${declaration.simpleName.asString()} class or removing $existingName class itself.",
                    existingProvider
                )
            }
        }
    }

    private fun KSAnnotated.validateAndMap(): Either<KSAnnotated, KSClassDeclaration> {

        val declaration = this

        if (!declaration.validate()) return declaration.left()

        // Checking if type is concrete class or not
        if (declaration !is KSClassDeclaration ||
            declaration.classKind != ClassKind.CLASS ||
            declaration.modifiers.contains(Modifier.ABSTRACT) ||
            declaration.modifiers.contains(Modifier.SEALED)
        ) {
            logger.error(
                message = "\nOnly concrete classes can be annotated with @${ConsiderForDowel::class.simpleName} annotation",
                symbol = declaration,
            )
            return declaration.left()
        }

        // Checking for private classes
        if (declaration.modifiers.contains(Modifier.PRIVATE)) {
            logger.error(
                message = "\nCannot create an instance for `${declaration.simpleName.asString()}` class: it is private in file.",
                declaration,
            )
            return declaration.left()
        }

        // Checking for private constructors
        val constructor = declaration.primaryConstructor!!
        if (constructor.modifiers.contains(Modifier.PRIVATE)) {
            logger.error(
                message = "\nCannot create an instance of class ${declaration.simpleName.asString()} as it's constructor is private.",
                constructor,
            )
            return declaration.left()
        }

        if (declaration.modifiers.contains(Modifier.INNER)) {
            logger.error(
                message = "\n@${ConsiderForDowel::class.simpleName} annotation can't be applied to inner classes",
                symbol = declaration,
            )
            return declaration.left()
        }

        return declaration.right()
    }

    /**
     * Validates that evey [KSClassDeclaration] extends
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] and that there is only a single
     * provider per type, annotated with @[ConsiderForDowel] annotation.
     *
     * Maps valid [KSType] to it's providing [KSClassDeclaration] and return the resultant as
     * [ProcessedConsiderForDowelSymbols].
     *
     * Logs error if any unexpected or invalid inputs are encountered
     */
    private fun List<KSClassDeclaration>.validateAndMap(): ProcessedConsiderForDowelSymbols {

        val declarations = this

        val mapOfTypes: MutableMap<KSType, KSClassDeclaration> = mutableMapOf()
        val invalidSymbols: MutableList<KSClassDeclaration> = mutableListOf()

        for (declaration in declarations) {

            val type: KSType? = declaration.findPreviewParamSuperType()

            if (type == null) {
                logger.error(
                    message = "\nClass ${declaration.qualifiedName!!.asString()} is annotated with @${ConsiderForDowel::class.simpleName}, but does not extend ${Names.previewParamProvider.canonicalName}.",
                    symbol = declaration,
                )
                invalidSymbols.add(declaration)
                continue
            }

            val existing = mapOfTypes[type]
            if (existing != null) {
                logger.error(
                    message = "\nMultiple classes providing preview params for type $type.\n" +
                        "Only a single class can be annotated with @${ConsiderForDowel::class.simpleName} per type.\n" +
                        "${existing.qualifiedName!!.asString()} & ${declaration.qualifiedName!!.asString()} both are annotated with @${ConsiderForDowel::class.simpleName} annotation for the same type.",
                    symbol = declaration,
                )
                continue
            }

            mapOfTypes[type] = declaration
        }

        return ProcessedConsiderForDowelSymbols(
            providers = mapOfTypes,
            invalidSymbols = invalidSymbols,
            validSymbols = declarations - invalidSymbols.toSet(),
        )
    }

    /**
     * Finds occurrence of [androidx.compose.ui.tooling.preview.PreviewParameterProvider] type
     * in the super types of passed [KSClassDeclaration] and returns it's generic type parameter's [KSType].
     *
     * **Note** : This only searches in the immediate super types, does not search recursively
     *
     * Returns null if no matches are found.
     */
    private fun KSClassDeclaration.findPreviewParamSuperType(): KSType? {

        val superTypes = this.superTypes

        for (superType in superTypes) {

            val resolved: KSType = superType.resolve()
            val isPreviewParamProvider = previewParamProviderType.isAssignableFrom(resolved)

            if (!isPreviewParamProvider) continue

            val args = resolved.arguments
            require(args.size == 1) { "androidx.compose.ui.tooling.preview.PreviewParameterProvider only has a single type parameter." }

            // Generic type parameter of androidx.compose.ui.tooling.preview.PreviewParameterProvider
            return args.first().type!!.resolve()
        }

        return null
    }

    /**
     * A container class to hold processed @[ConsiderForDowel] symbols.
     * @param validSymbols Valid symbols which fit the @[ConsiderForDowel] criteria
     * @param invalidSymbols Invalid symbols which didn't fit the @[ConsiderForDowel] criteria
     * @param providers Processed and mapped valid [KSAnnotated] symbols into [UserPredefinedParamProviders]
     */
    data class ProcessedConsiderForDowelSymbols(
        val validSymbols: List<KSClassDeclaration>,
        val invalidSymbols: List<KSAnnotated>,
        val providers: UserPredefinedParamProviders,
    )
}
