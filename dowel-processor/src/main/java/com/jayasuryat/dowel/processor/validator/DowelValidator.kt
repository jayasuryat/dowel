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
import com.google.devtools.ksp.symbol.Modifier
import com.google.devtools.ksp.validate
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.either.Either
import com.jayasuryat.either.left
import com.jayasuryat.either.right

/**
 * Validates if a symbol annotated with @[Dowel] annotation meets all of the necessary criteria.
 *
 * See [Dowel] for all of the applicable criteria.
 */
internal class DowelValidator(
    private val logger: KSPLogger,
) {

    /**
     * Validates if every symbol from the [symbols] list is fulling the @[Dowel] criteria or not.
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
            .forEach { element: Either<KSAnnotated, KSClassDeclaration> ->
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
                message = "\n@${Dowel::class.simpleName} annotation can only be applied to classes",
                symbol = declaration,
            )
            return declaration.left()
        }

        if (declaration.modifiers.contains(Modifier.ABSTRACT) ||
            declaration.modifiers.contains(Modifier.SEALED)
        ) {
            logger.error(
                message = "\n@${Dowel::class.simpleName} annotation can't be applied to an abstract classes",
                symbol = declaration,
            )
            return declaration.left()
        }

        if (declaration.modifiers.contains(Modifier.INNER)) {
            logger.error(
                message = "\n@${Dowel::class.simpleName} annotation can't be applied to inner classes",
                symbol = declaration,
            )
            return declaration.left()
        }

        if (declaration.modifiers.contains(Modifier.PRIVATE)) {
            logger.error(
                "\n@${Dowel::class.simpleName} cannot create an instance for `${declaration.simpleName.asString()}` class: it is private in file.",
                declaration,
            )
            return declaration.left()
        }

        val constructor = declaration.primaryConstructor!!
        if (constructor.modifiers.contains(Modifier.PRIVATE)) {
            logger.error(
                "\nCannot create an instance of class ${declaration.simpleName.asString()} as it's constructor is private.\n" +
                    "@${Dowel::class.simpleName} generates code based on the primary constructor of the annotated class, read more at ${Dowel::class.simpleName} annotation class's documentation.",
                constructor,
            )
            return declaration.left()
        }

        if (declaration.typeParameters.isNotEmpty()) {
            logger.error(
                message = "\n@${Dowel::class.simpleName} annotation can't be applied classes with generic type parameters.",
                symbol = declaration,
            )
            return declaration.left()
        }

        return declaration.right()
    }
}
