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

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName

internal object Names {

    val previewParamProvider: ClassName = ClassName(
        packageName = "androidx.compose.ui.tooling.preview",
        "PreviewParameterProvider"
    )

    val intRangeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "IntRange"
    )

    val floatRangeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "FloatRange"
    )

    val sizeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "Size"
    )

    val stateName: ClassName = ClassName(
        packageName = "androidx.compose.runtime",
        "State"
    )

    val flowName: ClassName = ClassName(
        packageName = "kotlinx.coroutines.flow",
        "Flow"
    )

    val sequenceName: ClassName = ClassName(
        packageName = "kotlin.sequences",
        "Sequence"
    )

    const val dowelClassNameSuffix: String = "PreviewParamProvider"
}

internal val KSClassDeclaration.dowelClassName: String
    get() = "${this.simpleName.asString()}${Names.dowelClassNameSuffix}"

internal val KSClassDeclaration.dowelListPropertyName: String
    get() = this.simpleName.asString().replaceFirstChar { char -> char.lowercaseChar() } + "List"
