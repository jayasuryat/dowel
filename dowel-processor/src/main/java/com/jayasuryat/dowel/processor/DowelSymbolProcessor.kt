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
import com.jayasuryat.dowel.processor.util.unsafeLazy

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
    private val visitor: KSVisitorVoid by unsafeLazy {
        DowelAnnotationVisitor(
            logger = logger,
            dowelGenerator = dowelGenerator,
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

        this.resolver = resolver

        val symbolsWithAnnotation: Sequence<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = Dowel::class.qualifiedName!!,
        )

        val (validSymbols: List<KSAnnotated>, invalidSymbols: List<KSAnnotated>) = symbolsWithAnnotation
            .partition { it.validate() }

        validSymbols.forEach { symbol -> symbol.accept(visitor, Unit) }

        return invalidSymbols
    }

    private class DowelAnnotationVisitor(
        private val logger: KSPLogger,
        private val dowelGenerator: DowelGenerator,
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {

            if (!classDeclaration.checkValidityAndLog(logger)) return

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
}
