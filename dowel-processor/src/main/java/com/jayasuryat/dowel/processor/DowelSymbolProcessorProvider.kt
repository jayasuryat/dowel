package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider

public class DowelSymbolProcessorProvider : SymbolProcessorProvider {

    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {

        return DowelSymbolProcessor(
            logger = environment.logger,
            codeGenerator = environment.codeGenerator,
        )
    }
}
