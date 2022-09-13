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
package com.jayasuryat.dowel.processor.util

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSNode
import com.squareup.kotlinpoet.FileSpec
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

@Suppress("NOTHING_TO_INLINE")
internal inline fun KSPLogger.logError(
    message: String,
    node: KSNode?,
): Nothing {
    this.error(message, node)
    throw Error(message)
}

internal fun <T> unsafeLazy(initializer: () -> T): Lazy<T> =
    lazy(
        mode = LazyThreadSafetyMode.NONE,
        initializer = initializer,
    )

internal fun FileSpec.writeTo(
    codeGenerator: CodeGenerator,
    dependencies: Dependencies,
) {
    val file = codeGenerator.createNewFile(dependencies, packageName, name)
    OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
}
