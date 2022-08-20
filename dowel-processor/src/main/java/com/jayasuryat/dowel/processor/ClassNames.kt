package com.jayasuryat.dowel.processor

import com.squareup.kotlinpoet.ClassName

internal object ClassNames {

    val previewParamProvider: ClassName = ClassName(
        packageName = "androidx.compose.ui.tooling.preview",
        "PreviewParameterProvider"
    )
}