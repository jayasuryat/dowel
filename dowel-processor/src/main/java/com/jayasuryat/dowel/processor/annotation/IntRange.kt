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
import com.jayasuryat.dowel.processor.ClassNames

internal data class IntRange(
    val start: Long,
    val end: Long,
) {

    companion object {

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
                annotation.shortName.asString() == ClassNames.intRangeName.simpleName
            }

            return validAnnotation?.asIntLimit(
                defaultStart = defaultStart,
                defaultEnd = defaultEnd,
            ) ?: IntRange(
                start = defaultStart,
                end = defaultEnd,
            )
        }

        private fun KSAnnotation.asIntLimit(
            defaultStart: Long,
            defaultEnd: Long,
        ): IntRange {

            val annotation = this

            val start = annotation.arguments
                .firstOrNull { it.name!!.asString() == "from" }
                ?.value as? Long
                ?: defaultStart

            val end = annotation.arguments
                .firstOrNull { it.name!!.asString() == "to" }
                ?.value as? Long
                ?: defaultEnd

            return IntRange(
                start = start,
                end = end,
            )
        }
    }
}
