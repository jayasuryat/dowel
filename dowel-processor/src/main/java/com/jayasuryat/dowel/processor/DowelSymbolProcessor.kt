package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.jayasuryat.dowel.annotation.Dowel

internal class DowelSymbolProcessor(
    private val logger: KSPLogger,
) : SymbolProcessor {

    private val visitor: KSVisitorVoid by lazy {
        DowelAnnotationVisitor(
            logger = logger,
        )
    }

    override fun process(resolver: Resolver): List<KSAnnotated> {

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
    ) : KSVisitorVoid() {

        override fun visitClassDeclaration(
            classDeclaration: KSClassDeclaration,
            data: Unit,
        ) {

            if (!classDeclaration.checkValidityAndLog(logger)) return
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

            if (
                declaration.modifiers.contains(Modifier.ABSTRACT) ||
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
