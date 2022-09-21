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
import com.google.devtools.ksp.symbol.KSType
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.annotation.FloatRange
import com.jayasuryat.dowel.processor.annotation.IntRange
import com.jayasuryat.dowel.processor.annotation.Size
import com.squareup.kotlinpoet.ClassName

/**
 * [ClassRepresentation] represents a class with all of it's properties mapped to concrete sealed
 * types to enable exhaustive checks while generating code.
 * The intent of this class is to map a [KSClassDeclaration] once to [ClassRepresentation] and
 * reuse it multiple times to generate code without having to resolve the type of a property
 * multiple times.
 *
 * This also acts as a reference for what all types are supported by Dowel for code generation.
 *
 * @param declaration The original [KSClassDeclaration] from which this representation has been mapped
 * @param parameters The list of all the properties of the represented class mapped to a concrete type
 *
 * @see [com.google.devtools.ksp.symbol.KSTypeReference.resolve]
 */
internal data class ClassRepresentation(
    val declaration: KSClassDeclaration,
    val parameters: List<Parameter>,
) {

    data class Parameter(
        val spec: ParameterSpec,
        val name: String,
        val isNullable: Boolean,
    )

    /**
     * A representation class denoting the type of a class with a concrete sealed type and
     * it's meta information like annotations, generic type parameters.
     */
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

        data class MapSpec(
            val size: Size,
            val keySpec: ParameterSpec,
            val valueSpec: ParameterSpec,
        ) : ParameterSpec

        data class FlowSpec(
            val elementSpec: ParameterSpec,
        ) : ParameterSpec

        data class PairSpec(
            val leftElementSpec: ParameterSpec,
            val rightElementSpec: ParameterSpec,
        ) : ParameterSpec

        data class FunctionSpec(
            val argumentsSize: Int,
            val isReturnTypeUnit: Boolean,
        ) : ParameterSpec

        data class SealedSpec(
            val subTypeSpecs: List<ParameterSpec>,
        ) : ParameterSpec

        data class EnumSpec(
            val enumDeclaration: KSClassDeclaration,
        ) : ParameterSpec

        data class ObjectSpec(
            val objectDeclaration: KSClassDeclaration,
        ) : ParameterSpec

        /**
         * Types which are annotated with @[Dowel] annotation.
         *
         * @see [BackedSpec]
         */
        data class DowelSpec(
            override val type: KSType,
            override val provider: ClassName,
            override val propertyName: String,
        ) : ParameterSpec, BackedSpec

        /**
         * Types for which user has provided a custom implementation of
         * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] through @[ConsiderForDowel]
         * annotation.
         *
         * @see [ConsiderForDowel]
         * @see [BackedSpec]
         */
        @Suppress("KDocUnresolvedReference")
        data class PreDefinedProviderSpec(
            override val type: KSType,
            override val provider: ClassName,
            override val propertyName: String,
        ) : ParameterSpec, BackedSpec

        /**
         * Types which are not directly supported by Dowel for code generation, but are nullable
         */
        object UnsupportedNullableSpec : ParameterSpec
    }

    /**
     * This interface represents a spec which is being backed by an implementation of
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider], and that implementation could
     * be coming from the generated code, or could be coming from user defined sources via @[Dowel]
     * or @[ConsiderForDowel] annotations respectively. For these specs there is no need to generate
     * code *by hand* as the existing provider could provide instances, and those instaces would
     * be used in constructing other objects.
     *
     * @property [type] : The type of object being provided
     * @property [provider] : The [ClassName] of the backing provider (implementation of
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider])
     * @property [propertyName] : A suggested name for a property to hold an instance of [provider]
     *
     * @see [Dowel]
     * @see [ConsiderForDowel]
     */
    @Suppress("KDocUnresolvedReference")
    interface BackedSpec {
        val type: KSType
        val provider: ClassName
        val propertyName: String
    }
}
