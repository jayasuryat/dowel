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

import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList

/**
 * Entry point into Kotlin Symbol Processor, creates and provides an instance of [SymbolProcessor]
 * i.e., [DowelSymbolProcessor].
 *
 * @see [DowelSymbolProcessor]
 * @see [Dowel]
 * @see [DowelList]
 */
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
