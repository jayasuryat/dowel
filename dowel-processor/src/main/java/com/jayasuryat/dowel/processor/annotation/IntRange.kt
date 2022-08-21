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
