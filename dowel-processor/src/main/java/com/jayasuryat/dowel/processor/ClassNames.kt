package com.jayasuryat.dowel.processor

import com.squareup.kotlinpoet.ClassName

internal object ClassNames {

    val previewParamProvider: ClassName = ClassName(
        packageName = "androidx.compose.ui.tooling.preview",
        "PreviewParameterProvider"
    )

    val intRangeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "IntRange"
    )

    val floatRangeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "FloatRange"
    )

    val sizeName: ClassName = ClassName(
        packageName = "androidx.annotation",
        "Size"
    )
}