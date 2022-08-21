package com.jayasuryat.dowel.processor.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.jayasuryat.dowel.processor.ClassNames

internal data class Size(
    val value: Long,
    val min: Long,
    val max: Long,
    val multiple: Long,
) {

    companion object {

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
                annotation.shortName.asString() == ClassNames.sizeName.simpleName
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
