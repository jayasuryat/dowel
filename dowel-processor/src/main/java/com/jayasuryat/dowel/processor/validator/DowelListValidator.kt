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
package com.jayasuryat.dowel.processor.validator

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.validate
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.either.Either
import com.jayasuryat.either.left
import com.jayasuryat.either.right

/**
 * Validates if a symbol annotated with @[DowelList] annotation meets all of the necessary criteria.
 *
 * See [DowelList] for all of the applicable criteria.
 */
internal class DowelListValidator(
    private val logger: KSPLogger,
) {

    /**
     * Validates if every symbol from the [symbols] list is fulling the @[DowelList] criteria or not.
     * Logs an error via the [KSPLogger] if any of the criteria is not satisfied.
     *
     * @return [ValidatedClassDeclarations]
     */
    fun validate(
        symbols: List<KSAnnotated>,
    ): ValidatedClassDeclarations {

        val valid: MutableList<KSClassDeclaration> = mutableListOf()
        val invalid: MutableList<KSAnnotated> = mutableListOf()

        symbols.map { symbol -> symbol.validateAndMap(logger = logger) }
            .forEach { element ->
                when (element) {
                    is Either.Left -> invalid.add(element.value)
                    is Either.Right -> valid.add(element.value)
                }
            }

        return ValidatedSymbols(
            valid = valid,
            invalid = invalid,
        )
    }

    private fun KSAnnotated.validateAndMap(
        logger: KSPLogger,
    ): Either<KSAnnotated, KSClassDeclaration> {

        val declaration = this

        if (!declaration.validate()) return declaration.left()

        if (declaration !is KSClassDeclaration ||
            declaration.classKind != ClassKind.CLASS
        ) {
            logger.error(
                message = "\n@${DowelList::class.simpleName} annotation can only be applied to classes",
                symbol = declaration,
            )
            return declaration.left()
        }

        val dowelAnnotation = declaration.annotations
            .find { it.shortName.asString() == Dowel::class.java.simpleName }

        if (dowelAnnotation == null) {
            logger.error(
                message = "\n@${DowelList::class.simpleName} annotation can only be applied to classes already annotated with @${Dowel::class.simpleName} annotation.",
                symbol = declaration,
            )
            return declaration.left()
        }

        return declaration.right()
    }
}
