package com.jayasuryat.dowel.processor.annotation

import com.google.devtools.ksp.symbol.KSAnnotation
import com.jayasuryat.dowel.processor.ClassNames

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
                annotation.shortName.asString() == ClassNames.floatRangeName.simpleName
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
