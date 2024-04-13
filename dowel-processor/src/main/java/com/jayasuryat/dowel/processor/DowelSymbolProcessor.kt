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
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.processor.generator.DowelGenerator
import com.jayasuryat.dowel.processor.generator.DowelListGenerator
import com.jayasuryat.dowel.processor.model.ExistingDeclarations
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviderMapper
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviderMapper.ProcessedConsiderForDowelSymbols
import com.jayasuryat.dowel.processor.model.UserPredefinedParamProviders
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.jayasuryat.dowel.processor.validator.*

/**
 * A [SymbolProcessor] implementation which intercepts declarations with @[Dowel], @[DowelList] and
 * @[ConsiderForDowel] annotations to trigger code generation for generating
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for the annotated classes.
 *
 * @see [Dowel]
 * @see [DowelList]
 * @see [ConsiderForDowel]
 */
@Suppress("KDocUnresolvedReference")
internal class DowelSymbolProcessor(
    private val logger: KSPLogger,
    private val codeGenerator: CodeGenerator,
) : SymbolProcessor {

    private lateinit var resolver: Resolver

    private val dowelValidator: DowelValidator by unsafeLazy {
        DowelValidator(
            logger = logger,
        )
    }
    private val dowelListValidator: DowelListValidator by unsafeLazy {
        DowelListValidator(
            logger = logger,
        )
    }
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
    private val declarations: ExistingDeclarations by unsafeLazy {
        ExistingDeclarations(
            resolver = resolver,
        )
    }

    /**
     * Entry point into processing of symbols, called by Kotlin Symbol Processing to run the processing task.
     */
    override fun process(resolver: Resolver): List<KSAnnotated> {

        this.resolver = resolver

        val considerForDowelSymbols: ProcessedConsiderForDowelSymbols =
            resolver.processConsiderForDowelSymbols()

        val invalidDowelSymbols: List<KSAnnotated> = resolver.processDowelSymbols(
            predefinedProviders = considerForDowelSymbols.providers,
            declarations = declarations,
        )

        val invalidDowelListSymbols: List<KSAnnotated> = resolver.processDowelListSymbols()

        // Returning all of the unprocessed or invalid symbols
        return invalidDowelSymbols + invalidDowelListSymbols + considerForDowelSymbols.invalidSymbols
    }

    /**
     * Processes symbols with @[Dowel] annotation and returns all of the invalid symbols.
     */
    private fun Resolver.processDowelSymbols(
        predefinedProviders: UserPredefinedParamProviders,
        declarations: ExistingDeclarations,
    ): List<KSAnnotated> {

        val resolver = this

        val symbols: List<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = Dowel::class.qualifiedName!!,
        ).toList()

        // Validating annotated symbols
        val validated: ValidatedClassDeclarations = dowelValidator.validate(symbols = symbols)

        val dowelGenerator: DowelGenerator = DowelGenerator.createInstance(
            predefinedProviders = predefinedProviders,
            declarations = declarations,
        )

        // Triggering code generation for valid symbols
        validated.valid.forEach { symbol ->
            dowelGenerator.generatePreviewParameterProviderFor(classDeclaration = symbol)
        }

        return validated.invalid
    }

    /**
     * Processes symbols with @[DowelList] annotation and returns all of the invalid symbols.
     */
    private fun Resolver.processDowelListSymbols(): List<KSAnnotated> {

        val resolver = this

        val symbols: List<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = DowelList::class.qualifiedName!!,
        ).toList()

        // Validating annotated symbols
        val validated: ValidatedClassDeclarations = dowelListValidator.validate(symbols)

        // Triggering code generation for valid symbols
        validated.valid.forEach { symbol ->
            dowelListGenerator.generateListPreviewParameterProviderFor(classDeclaration = symbol)
        }

        return validated.invalid
    }

    /**
     * Processes symbols with @[ConsiderForDowel] annotation and returns [ProcessedConsiderForDowelSymbols].
     * @see [ProcessedConsiderForDowelSymbols]
     */
    private fun Resolver.processConsiderForDowelSymbols(): ProcessedConsiderForDowelSymbols {

        val resolver = this

        val symbols: List<KSAnnotated> = resolver.getSymbolsWithAnnotation(
            annotationName = ConsiderForDowel::class.qualifiedName!!,
        ).toList()

        // Validating and mapping annotated symbols
        val processed: ProcessedConsiderForDowelSymbols = predefinedProviderMapper.map(symbols)

        // Logging warnings for redundant providers
        predefinedProviderMapper.logWarningForOverlappingDowelClasses(
            predefinedProviders = processed.providers,
            dowelDeclarations = processed.validSymbols,
        )

        return processed
    }

    /**
     * Short hand helper method to create an instance of [DowelAnnotationVisitor]
     */
    private fun DowelGenerator.Companion.createInstance(
        predefinedProviders: UserPredefinedParamProviders,
        declarations: ExistingDeclarations,
    ): DowelGenerator {
        return DowelGenerator(
            resolver = resolver,
            codeGenerator = codeGenerator,
            logger = logger,
            predefinedProviders = predefinedProviders,
            declarations = declarations,
        )
    }
}
