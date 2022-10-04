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
package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.processor.generator.DowelGenerator
import com.jayasuryat.dowel.processor.generator.DowelListGenerator
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviderMapper
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviders
import com.jayasuryat.dowel.processor.util.unsafeLazy

/**
 * A [SymbolProcessor] implementation which intercepts declarations with @[Dowel] and @[DowelList]
 * annotations to trigger code generation for generating
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for the annotated class.
 *
 * @see [Dowel]
 * @see [DowelList]
 */
@Suppress("KDocUnresolvedReference")
internal class DowelSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private lateinit var resolver: Resolver

    private val predefinedProviderMapper by unsafeLazy {
        UserPredefinedParamProviderMapper(
            resolver = resolver,
            logger = logger,
        )
    }

    private val dowelListGenerator: DowelListGenerator by unsafeLazy {
        DowelListGenerator(
            codeGenerator = codeGenerator,
        )
    }

    private val dowelListVisitor: KSVisitorVoid by unsafeLazy {
        DowelListAnnotationVisitor(
            logger = logger,
            generator = dowelListGenerator,
        )
    }

    /**
     * Entry point into processing of symbols, called by Kotlin Symbol Processing to run the processing task.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {

        this.resolver = resolver

        val invalidDowelSymbols: List<KSAnnotated> = resolver.processDowelSymbols()
        val invalidDowelListSymbols: List<KSAnnotated> = resolver.processDowelListSymbols()

        return invalidDowelSymbols + invalidDowelListSymbols
    }

    /**
     * Processes symbols for @[Dowel] annotation and returns all of the invalid symbols.
     */
    private fun Resolver.processDowelSymbols(): List<KSAnnotated> {

        /**
         * Resolves symbols annotated with @[ConsiderForDowel] annotation and maps them to
         * [UserPredefinedParamProviders] using the [predefinedProviderMapper] mapper class.
         */
        fun Resolver.getUserPredefinedParamProviders(): UserPredefinedParamProviders {

            val resolver = this

            val predefinedProviderSymbols: List<KSAnnotated> = resolver.getSymbolsWithAnnotation(
                annotationName = ConsiderForDowel::class.qualifiedName!!,
            ).toList()

            return predefinedProviderMapper.map(predefinedProviderSymbols)
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
            dowelSymbols: List<KSAnnotated>,
        ) {

            val declarations: Map<KSClassDeclaration, KSType> = dowelSymbols
                .filterIsInstance<KSClassDeclaration>()
                .associateWith { declaration -> declaration.asType(listOf()) }

            declarations.forEach { (declaration: KSClassDeclaration, type: KSType) ->

                val existingProvider: KSClassDeclaration? = predefinedProviders[type]

                if (existingProvider != null) {

                    val existingName = existingProvider.simpleName.asString()
                    val generatedName = declaration.dowelClassName

                    logger.warn(
                        "Duplicate/redundant providers found for type : $type, $existingName and $generatedName.\n" +
                            "$existingName will take precedence and will be used in outputs. $generatedName will be ignored.\n" +
                            "Consider removing the redundant @${Dowel::class.simpleName!!} annotation from the $existingName class."
                    )
                    logger.warn("", existingProvider)
                    logger.warn("", declaration)
                }
            }
        }

        val resolver = this

        val predefinedProviders: UserPredefinedParamProviders =
            resolver.getUserPredefinedParamProviders()

        val symbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = Dowel::class.qualifiedName!!,
        )

        val (validSymbols: List<KSAnnotated>, invalidSymbols: List<KSAnnotated>) =
            symbols.partition { it.validate() }

        logWarningForOverlappingDowelClasses(
            predefinedProviders = predefinedProviders,
            dowelSymbols = validSymbols,
        )

        val visitor: KSVisitorVoid = DowelAnnotationVisitor.createInstance(
            predefinedProviders = predefinedProviders,
        )

        validSymbols.forEach { symbol -> symbol.accept(visitor, Unit) }

        return invalidSymbols
    }

    /**
     * Processes symbols for @[DowelList] annotation and returns all of the invalid symbols.
     */
    private fun Resolver.processDowelListSymbols(): List<KSAnnotated> {

        val resolver = this

        val symbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = DowelList::class.qualifiedName!!,
        )

        val (validSymbols: List<KSAnnotated>, invalidSymbols: List<KSAnnotated>) =
            symbols.partition { it.validate() }

        validSymbols.forEach { symbol -> symbol.accept(dowelListVisitor, Unit) }

        return invalidSymbols
    }

    // region : Visitors
    /**
     * Validates if a class annotated with @[Dowel] annotation meets all of the necessary criteria.
     * Triggers code generation if a class is validated to be appropriate, otherwise logs an error
     * with appropriate message.
     *
     * See [Dowel] for all of the applicable criteria.
     */
    private class DowelAnnotationVisitor(
        private val logger: KSPLogger,
        private val dowelGenerator: DowelGenerator,
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {

            if (!classDeclaration.checkValidityAndLog(logger)) return

            // Triggering code generation
            dowelGenerator.generatePreviewParameterProviderFor(
                classDeclaration = classDeclaration,
            )
        }

        private fun KSClassDeclaration.checkValidityAndLog(
            logger: KSPLogger,
        ): Boolean {

            val declaration = this

            if (declaration.classKind != ClassKind.CLASS) {
                logger.error(
                    message = "\n@${Dowel::class.simpleName} annotation can only be applied to classes",
                    symbol = declaration,
                )
                return false
            }

            if (declaration.modifiers.contains(Modifier.ABSTRACT) ||
                declaration.modifiers.contains(Modifier.SEALED)
            ) {
                logger.error(
                    message = "\n@${Dowel::class.simpleName} annotation can't be applied to an abstract classes",
                    symbol = declaration,
                )
                return false
            }

            if (declaration.modifiers.contains(Modifier.INNER)) {
                logger.error(
                    message = "\n@${Dowel::class.simpleName} annotation can't be applied to inner classes",
                    symbol = declaration,
                )
                return false
            }

            if (declaration.modifiers.contains(Modifier.PRIVATE)) {
                logger.error(
                    "\n@${Dowel::class.simpleName} cannot create an instance for `${declaration.simpleName.asString()}` class: it is private in file.",
                    declaration,
                )
                return false
            }

            val constructor = declaration.primaryConstructor!!
            if (constructor.modifiers.contains(Modifier.PRIVATE)) {
                logger.error(
                    "\nCannot create an instance of class ${declaration.simpleName.asString()} as it's constructor is private.\n" +
                        "@${Dowel::class.simpleName} generates code based on the primary constructor of the annotated class, read more at ${Dowel::class.simpleName} annotation class's documentation.",
                    constructor,
                )
                return false
            }

            if (declaration.typeParameters.isNotEmpty()) {
                logger.error(
                    message = "\n@${Dowel::class.simpleName} annotation can't be applied classes with generic type parameters.",
                    symbol = declaration,
                )
                return false
            }

            return true
        }

        companion object
    }

    /**
     * Short hand helper method to create an instance of [DowelAnnotationVisitor]
     */
    private fun DowelAnnotationVisitor.Companion.createInstance(
        predefinedProviders: UserPredefinedParamProviders,
    ): DowelAnnotationVisitor {
        return DowelAnnotationVisitor(
            logger = logger,
            dowelGenerator = DowelGenerator(
                resolver = resolver,
                codeGenerator = codeGenerator,
                logger = logger,
                predefinedProviders = predefinedProviders
            )
        )
    }

    /**
     * Validates if a class annotated with @[DowelList] annotation meets all of the necessary criteria.
     * Triggers code generation if a class is validated to be appropriate, otherwise logs an error
     * with appropriate message.
     *
     * See [DowelList] for all of the applicable criteria.
     */
    private class DowelListAnnotationVisitor(
        private val logger: KSPLogger,
        private val generator: DowelListGenerator,
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {

            if (!classDeclaration.checkValidityAndLog(logger)) return

            // Triggering code generation
            generator.generateListPreviewParameterProviderFor(
                classDeclaration = classDeclaration,
            )
        }

        private fun KSClassDeclaration.checkValidityAndLog(
            logger: KSPLogger,
        ): Boolean {

            val declaration = this

            val dowelAnnotation = declaration.annotations
                .find { it.shortName.asString() == Dowel::class.java.simpleName }

            if (dowelAnnotation == null) {
                logger.error(
                    message = "\n@${DowelList::class.simpleName} annotation can only be applied to classes already annotated with @${Dowel::class.simpleName} annotation.",
                    symbol = declaration,
                )
                return false
            }

            return true
        }
    }
    // endregion
}
