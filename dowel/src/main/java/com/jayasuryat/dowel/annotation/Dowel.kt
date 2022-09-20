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
package com.jayasuryat.dowel.annotation

import com.jayasuryat.dowel.internal.DowelInternal

/**
 * This is the primary entry-point into Dowel.
 *
 * An annotation class for triggering the generation of PreviewParameterProvider for the annotated class
 * ([androidx.compose.ui.tooling.preview.PreviewParameterProvider]).
 *
 * The generated PreviewParameterProvider will have [count] number of objects in the sequence of
 * values, and the objects would be constructed using the primary constructor of the annotated class.
 * All properties with default values would be ignored, and nullable types would be null randomly.
 *
 * The annotated class must be a concrete class, class should not have any generic type parameters,
 * and all of the properties listed in the primary constructor can only be of the following types:
 * * Primitives ([Int], [Long], [Float], [Double], [Char], [Boolean], [String])
 * * [androidx.compose.runtime.State]
 * * [kotlinx.coroutines.flow.Flow]
 * * Functional types (high-order functions)
 * * @[Dowel] classes (@[Dowel] classes can be nested. A @[Dowel] annotated class can have
 *   properties of type of classes which are again annotated with @[Dowel])
 * * Types for which a user-defined PreviewParameterProvider exist (via the @[ConsiderForDowel]
 * annotation)
 * * Sealed types
 * * [Enum]
 * * [List]
 * * [Map]
 * * [Pair]
 * * Nullable types
 *
 * Properties with default values can have any type, as they are not considered for generation.
 *
 * Properties with **unsupported** types which are nullable are allowed, and the generated value would
 * always be null. Properties with properly supported types which are nullable may have an appropriate
 * value or a null value generated randomly.
 *
 * Types in the above mentioned list having generic type parameters (like [List] and [Map]) can only
 * have @[Dowel] supported types as their type parameters.
 * Like List&lt;String&gt;, Map&lt;String, @[Dowel] class&gt;.
 *
 * As far as a type is in this supported list, there are no practical limitations on how many times they may be nested.
 * Like List&lt;Map&lt;String, List&lt;@[Dowel] class&gt;&gt;&gt;
 *
 * **Note** : More meta information about a property can be given to Dowel using [androidx.annotation]
 * annotations. Currently, following are the supported annotations:
 * * [androidx.annotation.IntRange]
 * * [androidx.annotation.FloatRange]
 * * [androidx.annotation.Size]
 *
 * @param [count] Number of items in the generated sequence of items
 * @see [DowelList]
 * @see [ConsiderForDowel]
 */
@Suppress("KDocUnresolvedReference")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class Dowel(
    val count: Int = DEFAULT_COUNT,
) {

    public companion object {

        private const val DEFAULT_COUNT: Int = 5

        @DowelInternal
        public const val COUNT_PROPERTY_NAME: String = "count"
    }
}
