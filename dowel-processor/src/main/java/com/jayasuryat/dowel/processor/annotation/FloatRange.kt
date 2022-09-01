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

internal data class FloatRange(
    val start: Double,
    val end: Double,
) {

    companion object {

        fun find(
            annotations: List<KSAnnotation>,
            defaultStart: Double,
            defaultEnd: Double,
        ): FloatRange {

            if (annotations.isEmpty()) return FloatRange(
                start = defaultStart,
                end = defaultEnd,
            )

            val validAnnotation = annotations.firstOrNull { annotation ->
                annotation.shortName.asString() == Names.floatRangeName.simpleName
            }

            return validAnnotation?.asFloatLimit(
                defaultStart = defaultStart,
                defaultEnd = defaultEnd,
            ) ?: FloatRange(
                start = defaultStart,
                end = defaultEnd,
            )
        }

        private fun KSAnnotation.asFloatLimit(
            defaultStart: Double,
            defaultEnd: Double,
        ): FloatRange {

            val annotation = this

            val start = annotation.arguments
                .firstOrNull { it.name!!.asString() == "from" }
                ?.value as? Double
                ?: defaultStart

            val end = annotation.arguments
                .firstOrNull { it.name!!.asString() == "to" }
                ?.value as? Double
                ?: defaultEnd

            return FloatRange(
                start = start,
                end = end,
            )
        }
    }
}
