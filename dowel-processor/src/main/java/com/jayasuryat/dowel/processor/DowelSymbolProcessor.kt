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
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.processor.generator.DowelGenerator
import com.jayasuryat.dowel.processor.generator.DowelListGenerator
import com.jayasuryat.dowel.processor.util.unsafeLazy

/**
 * A [SymbolProcessor] implementation which intercepts declarations with @[Dowel] and @[DowelList]
 * annotations to trigger code generation for generating
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for the annotated class.
 *
 * @see [Dowel]
 * @see [DowelList]
 */
internal class DowelSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private lateinit var resolver: Resolver

    private val dowelGenerator: DowelGenerator by unsafeLazy {
        DowelGenerator(
            resolver = resolver,
            codeGenerator = codeGenerator,
            logger = logger,
        )
    }
    private val dowelListGenerator: DowelListGenerator by unsafeLazy {
        DowelListGenerator(
            codeGenerator = codeGenerator,
        )
    }

    private val dowelVisitor: KSVisitorVoid by unsafeLazy {
        DowelAnnotationVisitor(
            logger = logger,
            dowelGenerator = dowelGenerator,
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

        val invalidDowelSymbols: List<KSAnnotated> = resolver.processSymbol(
            annotationName = Dowel::class.qualifiedName!!,
            visitor = dowelVisitor,
        )

        val invalidDowelListSymbols: List<KSAnnotated> = resolver.processSymbol(
            annotationName = DowelList::class.qualifiedName!!,
            visitor = dowelListVisitor,
        )

        return invalidDowelSymbols + invalidDowelListSymbols
    }

    /**
     * Processes symbols with the passed information and returns all of the invalid symbols.
     */
    private fun Resolver.processSymbol(
        annotationName: String,
        visitor: KSVisitorVoid,
    ): List<KSAnnotated> {

        val resolver = this

        val symbols: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = annotationName,
        )

        val (validSymbols: List<KSAnnotated>, invalidSymbols: List<KSAnnotated>) =
            symbols.partition { it.validate() }

        validSymbols.forEach { symbol -> symbol.accept(visitor, Unit) }

        return invalidSymbols
    }

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
                    message = "@${Dowel::class.qualifiedName} annotation can only be applied to classes",
                    symbol = declaration,
                )
                return false
            }

            if (declaration.modifiers.contains(Modifier.ABSTRACT) ||
                declaration.modifiers.contains(Modifier.SEALED)
            ) {
                logger.error(
                    message = "@${Dowel::class.qualifiedName} annotation can't be applied to an abstract class",
                    symbol = declaration,
                )
                return false
            }

            return true
        }
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
                    message = "@${DowelList::class.qualifiedName} annotation can only be applied to classes already annotated with @${Dowel::class.qualifiedName} annotation.",
                    symbol = declaration,
                )
                return false
            }

            return true
        }
    }
}
