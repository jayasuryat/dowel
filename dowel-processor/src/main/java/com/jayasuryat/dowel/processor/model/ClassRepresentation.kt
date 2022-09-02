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
package com.jayasuryat.dowel.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.processor.annotation.FloatRange
import com.jayasuryat.dowel.processor.annotation.IntRange
import com.jayasuryat.dowel.processor.annotation.Size

internal data class ClassRepresentation(
    val declaration: KSClassDeclaration,
    val parameters: List<Parameter>,
) {

    data class Parameter(
        val spec: ParameterSpec,
        val name: String,
        val hasDefault: Boolean,
    )

    sealed interface ParameterSpec {

        // region : Primitives
        data class IntSpec(
            val range: IntRange,
        ) : ParameterSpec

        data class LongSpec(
            val range: IntRange,
        ) : ParameterSpec

        data class FloatSpec(
            val range: FloatRange,
        ) : ParameterSpec

        data class DoubleSpec(
            val range: FloatRange,
        ) : ParameterSpec

        object CharSpec : ParameterSpec

        object BooleanSpec : ParameterSpec

        data class StringSpec(
            val size: Size,
        ) : ParameterSpec
        // endregion

        data class StateSpec(
            val elementSpec: ParameterSpec,
        ) : ParameterSpec

        data class ListSpec(
            val size: Size,
            val elementSpec: ParameterSpec,
        ) : ParameterSpec

        data class FlowSpec(
            val elementSpec: ParameterSpec,
        ) : ParameterSpec

        data class FunctionSpec(
            val argumentsSize: Int,
            val isReturnTypeUnit: Boolean,
        ) : ParameterSpec

        data class EnumSpec(
            val enumDeclaration: KSClassDeclaration,
        ) : ParameterSpec

        data class DowelSpec(
            val declaration: KSClassDeclaration,
        ) : ParameterSpec
    }
}
