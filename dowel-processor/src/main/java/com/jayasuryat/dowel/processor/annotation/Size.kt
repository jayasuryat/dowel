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

internal data class Size(
    val value: Long,
    val min: Long,
    val max: Long,
    val multiple: Long,
) {

    companion object {

        // TODO: Values of this annotation are never null, so there should be better way to handle defaults

        fun find(
            annotations: List<KSAnnotation>,
            defaultValue: Long,
            defaultMin: Long,
            defaultMax: Long,
            defaultMultiple: Long = 1,
        ): Size {

            if (annotations.isEmpty()) return Size(
                value = defaultValue,
                min = defaultMin,
                max = defaultMax,
                multiple = defaultMultiple,
            )

            val validAnnotation = annotations.firstOrNull { annotation ->
                annotation.shortName.asString() == Names.sizeName.simpleName
            }

            return validAnnotation?.asSize(
                defaultValue = defaultValue,
                defaultMin = defaultMin,
                defaultMax = defaultMax,
                defaultMultiple = defaultMultiple,
            ) ?: Size(
                value = defaultValue,
                min = defaultMin,
                max = defaultMax,
                multiple = defaultMultiple,
            )
        }

        private fun KSAnnotation.asSize(
            defaultValue: Long,
            defaultMin: Long,
            defaultMax: Long,
            defaultMultiple: Long,
        ): Size {

            val annotation = this

            val value = annotation.arguments
                .firstOrNull { it.name!!.asString() == "value" }
                ?.value as? Long
                ?: defaultValue

            val min = annotation.arguments
                .firstOrNull { it.name!!.asString() == "min" }
                ?.value as? Long
                ?: defaultMin

            val max = annotation.arguments
                .firstOrNull { it.name!!.asString() == "max" }
                ?.value as? Long
                ?: defaultMax

            val multiple = annotation.arguments
                .firstOrNull { it.name!!.asString() == "multiple" }
                ?.value as? Long
                ?: defaultMultiple

            return Size(
                value = value,
                min = min,
                max = max,
                multiple = multiple,
            )
        }
    }
}
