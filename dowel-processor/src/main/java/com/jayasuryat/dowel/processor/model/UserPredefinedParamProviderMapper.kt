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
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.util.unsafeLazy

/**
 * Mapper class to map [List]&lt;[KSAnnotated]&gt; to [UserPredefinedParamProviders].
 *
 * This mapper assumes that all of the [KSAnnotated] are annotated with [ConsiderForDowel]
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
    ): UserPredefinedParamProviders {

        return declarations
            .validateAndMapAsClassDeclarations()
            .validateAndMap()
    }

    /**
     * Validates that evey [KSAnnotated] in the list is of type [KSClassDeclaration] and is a
     * concrete class. Maps valid symbols to [KSClassDeclaration] and returns the resultant.
     *
     * Logs error if any unexpected or invalid inputs are encountered
     */
    private fun List<KSAnnotated>.validateAndMapAsClassDeclarations(): List<KSClassDeclaration> {

        fun KSAnnotated.validateAndLogIfError(): Boolean {

            val declaration = this

            // Checking if type is concrete class or not
            if (declaration !is KSClassDeclaration || declaration.classKind != ClassKind.CLASS) {
                logger.error(
                    message = "Only concrete classes can be annotated with @${ConsiderForDowel::class.simpleName} annotation",
                    symbol = declaration,
                )
                return false
            }

            // Checking for private classes
            if (declaration.modifiers.contains(Modifier.PRIVATE)) {
                logger.error(
                    message = " \n@${Dowel::class.simpleName} cannot create an instance for `${declaration.simpleName.asString()}` class: it is private in file.",
                    declaration,
                )
                return false
            }

            // Checking for private constructors
            val constructor = declaration.primaryConstructor!!
            if (constructor.modifiers.contains(Modifier.PRIVATE)) {
                logger.error(
                    message = " \nCannot create an instance of class ${declaration.simpleName.asString()} as it's constructor is private.",
                    constructor,
                )
                return false
            }

            return true
        }

        return this.mapNotNull { declaration ->
            val isValid: Boolean = declaration.validateAndLogIfError()
            if (isValid) declaration as KSClassDeclaration else null
        }
    }

    /**
     * Validates that evey [KSClassDeclaration] extends
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] and that there is only a single
     * provider per type, annotated with @[ConsiderForDowel] annotation.
     *
     * Maps valid [KSType] to it's providing [KSClassDeclaration] and return the resultant.
     *
     * Logs error if any unexpected or invalid inputs are encountered
     */
    private fun List<KSClassDeclaration>.validateAndMap(): Map<KSType, KSClassDeclaration> {

        val declarations = this

        val mapOfTypes: MutableMap<KSType, KSClassDeclaration> = mutableMapOf()

        for (declaration in declarations) {

            val type: KSType? = declaration.findPreviewParamSuperType()

            if (type == null) {
                logger.error(
                    message = "Class ${declaration.qualifiedName!!.asString()} is annotated with @${ConsiderForDowel::class.simpleName}, but does not extend ${Names.previewParamProvider.canonicalName}.",
                    symbol = declaration,
                )
                continue
            }

            val existing = mapOfTypes[type]
            if (existing != null) {
                logger.error(
                    message = "Multiple classes providing preview params for type $type.\n" +
                        "Only a single class can be annotated with @${ConsiderForDowel::class.simpleName} per type.\n" +
                        "${existing.qualifiedName!!.asString()} & ${declaration.qualifiedName!!.asString()} both are annotated with @${ConsiderForDowel::class.simpleName} annotation for the same type.",
                    symbol = declaration,
                )
                continue
            }

            mapOfTypes[type] = declaration
        }

        return mapOfTypes
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
}
