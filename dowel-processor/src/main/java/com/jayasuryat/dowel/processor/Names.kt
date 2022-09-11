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
package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.processor.util.asClassName
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.asTypeName

internal object Names {

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

    val stateName: ClassName = ClassName(
        packageName = "androidx.compose.runtime",
        "State"
    )

    val flowName: ClassName = ClassName(
        packageName = "kotlinx.coroutines.flow",
        "Flow"
    )

    val listName: ClassName = List::class.asTypeName()

    val sequenceName: ClassName = Sequence::class.asTypeName()

    const val dowelClassNameSuffix: String = "PreviewParamProvider"
    const val dowelListClassNameSuffix: String = "ListPreviewParamProvider"
}

/**
 * Returns a *relative* dot separated class name like Map.Entry or List, useful when dealing with
 * nested classes
 */
internal val ClassName.relativeClassName: String
    get() {
        val packageName = this.packageName
        return this.canonicalName
            .substring(packageName.length)
            .removePrefix(".")
    }
internal val KSClassDeclaration.relativeClassName: String
    get() = this.asClassName().relativeClassName

/**
 * Returns a string which could be used as the name of a Dowel generated PreviewParameterProvider
 * class.
 *
 * The string would be in the following format '&lt;Name of KSClassDeclaration&gt;PreviewParamProvider'
 * where the name would be considered based on the nesting of class. For example
 * MapEntryPreviewParamProvider for Map.Entry class, and PersonPreviewParamProvider for Person class
 */
internal val ClassName.dowelClassName: String
    get() {
        val relativeName = this.relativeClassName
        val dotRemoved = relativeName.replace(".", "")
        return "$dotRemoved${Names.dowelClassNameSuffix}"
    }
internal val KSClassDeclaration.dowelClassName: String
    get() = this.asClassName().dowelClassName

/**
 * Returns a string which could be used as the name of a Dowel generated List-PreviewParameterProvider
 * class.
 *
 * The string would be in the following format '&lt;Name of KSClassDeclaration&gt;ListPreviewParamProvider'
 * where the name would be considered based on the nesting of class. For example
 * MapEntryListPreviewParamProvider for Map.Entry class, and PersonListPreviewParamProvider for Person class
 */
internal val ClassName.dowelListClassName: String
    get() {
        val relativeName = this.relativeClassName
        val dotRemoved = relativeName.replace(".", "")
        return "$dotRemoved${Names.dowelListClassNameSuffix}"
    }
internal val KSClassDeclaration.dowelListClassName: String
    get() = this.asClassName().dowelListClassName

/**
 * Returns a string which could be used as the name of a property in Dowel generated
 * PreviewParameterProvider class to store references of other [List] types.
 *
 * The string would be in the following format '&lt;Relative name of class&gt;List'
 * where the name would be considered based on the nesting of class. For example
 * mapEntryList for Map.Entry class, and personList for Person class
 */
internal val ClassName.dowelListPropertyName: String
    get() = this.relativeClassName.replaceFirstChar { char -> char.lowercaseChar() } + "List"
internal val KSClassDeclaration.dowelListPropertyName: String
    get() = this.asClassName().dowelListPropertyName
