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
 * An adapter class to represent and hold values coming from the [androidx.annotation.Size]
 * annotation.
 * @param value represents the 'value' value of [androidx.annotation.Size]
 * @param min represents the 'min' value of [androidx.annotation.Size]
 * @param max represents the 'max' value of [androidx.annotation.Size]
 * @param multiple represents the 'multiple' value of [androidx.annotation.Size]
 */
@Suppress("KDocUnresolvedReference")
internal data class Size(
    val value: Long,
    val min: Long,
    val max: Long,
    val multiple: Long,
) {

    companion object {

        /**
         * Finds [androidx.annotation.Size] annotation from passed [annotations] and maps
         * respective values to the [Size] holder class and returns it. In case the
         * [androidx.annotation.Size] annotation is not found, resorts to the passed default values.
         */
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

            return validAnnotation?.asSize() ?: Size(
                value = defaultValue,
                min = defaultMin,
                max = defaultMax,
                multiple = defaultMultiple,
            )
        }

        private fun KSAnnotation.asSize(): Size {

            val annotation = this

            val value = annotation.arguments
                .first { it.name!!.asString() == "value" }
                .value as Long

            val min = annotation.arguments
                .first { it.name!!.asString() == "min" }
                .value as Long

            val max = annotation.arguments
                .first { it.name!!.asString() == "max" }
                .value as Long

            val multiple = annotation.arguments
                .first { it.name!!.asString() == "multiple" }
                .value as Long

            return Size(
                value = value,
                min = min,
                max = max,
                multiple = multiple,
            )
        }
    }
}
