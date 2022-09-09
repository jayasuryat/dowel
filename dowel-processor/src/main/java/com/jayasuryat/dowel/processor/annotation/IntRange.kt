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
package com.jayasuryat.dowel.processor.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.jayasuryat.dowel.processor.Names

/**
 * An adapter class to represent and hold values coming from the [androidx.annotation.IntRange]
 * annotation.
 * @param start represents the 'from' value of [androidx.annotation.IntRange]
 * @param end represents the 'to' value of [androidx.annotation.IntRange]
 */
@Suppress("KDocUnresolvedReference")
internal data class IntRange(
    val start: Long,
    val end: Long,
) {

    companion object {

        /**
         * Finds [androidx.annotation.IntRange] annotation from passed [annotations] and maps
         * respective values to the [IntRange] holder class and returns it. In case the
         * [androidx.annotation.IntRange] annotation is not found, resorts to the passed default values.
         */
        fun find(
            annotations: List<KSAnnotation>,
            defaultStart: Long,
            defaultEnd: Long,
        ): IntRange {

            if (annotations.isEmpty()) return IntRange(
                start = defaultStart,
                end = defaultEnd,
            )

            val validAnnotation = annotations.firstOrNull { annotation ->
                annotation.shortName.asString() == Names.intRangeName.simpleName
            }

            return validAnnotation?.asIntLimit() ?: IntRange(
                start = defaultStart,
                end = defaultEnd,
            )
        }

        private fun KSAnnotation.asIntLimit(): IntRange {

            val annotation = this

            val start = annotation.arguments
                .first { it.name!!.asString() == "from" }
                .value as Long

            val end = annotation.arguments
                .first { it.name!!.asString() == "to" }
                .value as Long

            return IntRange(
                start = start,
                end = end,
            )
        }
    }
}
