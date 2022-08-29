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

internal object DefaultRange {

    internal const val DEFAULT_INT_MIN: Long = 0
    internal const val DEFAULT_INT_MAX: Long = 100

    internal const val DEFAULT_LONG_MIN: Long = 0
    internal const val DEFAULT_LONG_MAX: Long = 100000

    internal const val DEFAULT_FLOAT_MIN: Double = 0.0
    internal const val DEFAULT_FLOAT_MAX: Double = 100.0

    internal const val DEFAULT_DOUBLE_MIN: Double = 0.0
    internal const val DEFAULT_DOUBLE_MAX: Double = 100.0

    internal const val DEFAULT_STRING_LEN_MIN: Long = 0L
    internal const val DEFAULT_STRING_LEN_MAX: Long = 50L
    internal const val DEFAULT_STRING_LEN_VALUE: Long = 30L

    internal const val DEFAULT_LIST_LEN_MIN: Long = 5L
    internal const val DEFAULT_LIST_LEN_MAX: Long = 10L
    internal const val DEFAULT_LIST_LEN_VALUE: Long = 5L
}
