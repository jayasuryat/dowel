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

import com.jayasuryat.dowel.annotation.internal.DowelInternal

/**
 * This annotation builds on top of what already @[Dowel] annotation is doing.
 *
 * Generates a PreviewParameterProvider of type List&lt;T&gt; where T is the class annotated
 * with [DowelList] annotation. Rest of the behavior is same as the @[Dowel] annotation.
 *
 * **Note** : Only classes already annotated with @[Dowel] can be annotated with @[DowelList].
 *
 * @param [count] Number of items in the generated sequence of type List&lt;T&gt;.
 * @see [Dowel]
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class DowelList(
    val count: Int = DEFAULT_COUNT,
) {

    public companion object {

        private const val DEFAULT_COUNT: Int = 5

        @DowelInternal
        public const val COUNT_PROPERTY_NAME: String = "count"
    }
}
