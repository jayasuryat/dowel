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
package com.jayasuryat.dowel.processor.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.processor.*
import com.jayasuryat.dowel.processor.util.writeTo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Generates a file containing an implementation of
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for List&lt;[KSClassDeclaration]&gt;.
 * This class generates on top of what [DowelGenerator] is already generating.
 *
 * The generated file would have the same package as the [KSClassDeclaration]
 * but would be in the generated sources. File name would be <Name of [KSClassDeclaration]>ListPreviewParamProvider.kt
 *
 * @see [DowelGenerator]
 */
@Suppress("KDocUnresolvedReference")
internal class DowelListGenerator(
    private val codeGenerator: CodeGenerator,
) {

    /**
     * Generates an implementation of [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
     * for List&lt;[classDeclaration]&gt;.
     */
    fun generateListPreviewParameterProviderFor(
        classDeclaration: KSClassDeclaration,
    ) {

        val fileSpec: FileSpec = FileSpec
            .builder(
                packageName = classDeclaration.packageName.asString(),
                fileName = classDeclaration.dowelListClassName,
            )
            .addPreviewParamProvider(classDeclaration = classDeclaration)
            .build()

        // Creating a file with specified specs and flushing code into it
        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(
                // This is not an aggregating output as it only depends on the KSClassDeclaration
                // of annotated class. To be more precise, it only depends on the class generated
                // by the @Dowel annotation for that class. Which is more or less, the same thing.
                aggregating = false,
                classDeclaration.containingFile!!
            )
        )
    }

    /**
     * Method responsible for generating all of the code inside the implementation of
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for List&lt;[classDeclaration]&gt;.
     */
    private fun FileSpec.Builder.addPreviewParamProvider(
        classDeclaration: KSClassDeclaration,
    ): FileSpec.Builder {

        // Annotated class's class name
        val declarationClassName = classDeclaration.toClassName()
        val outputClassName = declarationClassName.dowelListClassName

        val listType = Names.listName.parameterizedBy(declarationClassName)

        // Super-type of the generated class
        val outputSuperType = Names.previewParamProvider.parameterizedBy(listType)

        // Number of objects to be generated as part of the "values" sequence property
        val sequenceSize: Int = classDeclaration.annotations
            .first { it.shortName.asString() == DowelList::class.java.simpleName }
            .arguments
            .first { it.name!!.asString() == DowelList.Companion.COUNT_PROPERTY_NAME }
            .value as Int

        val classSpec: TypeSpec = TypeSpec
            .classBuilder(outputClassName)
            .addSuperinterface(outputSuperType)
            .addDowelListProperty(declaration = classDeclaration)
            .addValuesProperty(
                declaration = classDeclaration,
                instanceCount = sequenceSize,
            ).build()

        this.addType(typeSpec = classSpec)

        return this
    }

    /**
     * Adds private property inside the generated class which help in simplifying code generation.
     *
     * Instead of generating the instantiation code of list of objects by *hand*, their respective
     * (generated) PreviewParameterProviders are reused to provide instances of those respective types.
     *
     * This method adds instances of such PreviewParameterProviders as private properties inside the
     * generated class.
     */
    private fun TypeSpec.Builder.addDowelListProperty(
        declaration: KSClassDeclaration,
    ): TypeSpec.Builder {

        val declarationName = declaration.toClassName()
        val declarationListType = Names.listName.parameterizedBy(declarationName)

        val values = PropertySpec.builder(
            name = declaration.getDowelListPropertyName,
            type = declarationListType,
            modifiers = listOf(KModifier.PRIVATE),
        ).initializer("${declaration.dowelClassName}().values.toList()")
            .build()

        this.addProperty(values)

        return this
    }

    /**
     * Adds the overridden "values" property of the [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
     * class with a [Sequence] of values of type represented by List&lt;[representation]&gt;.
     */
    private fun TypeSpec.Builder.addValuesProperty(
        declaration: KSClassDeclaration,
        instanceCount: Int,
    ): TypeSpec.Builder {

        val declarationName = declaration.toClassName()
        val listType = Names.listName.parameterizedBy(declarationName)

        val propertyType = Names.sequenceName.parameterizedBy(listType)

        val randomMember = MemberName(
            packageName = "kotlin.random",
            simpleName = "Random"
        )

        val propName = declaration.getDowelListPropertyName

        // Building a sequence by extracting random values out of  PreviewParameterProvider
        val initializer: CodeBlock = CodeBlock.builder()
            .addStatement("sequence {")
            .withIndent {
                addStatement("repeat(%L) {", instanceCount)
                withIndent {
                    add(
                        "yield(%L.shuffled().take(%M.nextInt(%L.size)))\n",
                        propName,
                        randomMember,
                        propName,
                    )
                }
                addStatement("}")
            }
            .addStatement("}")
            .build()

        val valuesProperty: PropertySpec = PropertySpec.builder(
            name = DOWEL_LIST_PROP_NAME,
            type = propertyType,
            modifiers = listOf(KModifier.OVERRIDE),
        ).initializer(initializer)
            .build()

        this.addProperty(valuesProperty)

        return this
    }

    companion object {

        private val KSClassDeclaration.getDowelListPropertyName: String
            get() {
                val relativeName = this.relativeClassName
                    .replace(".", "")
                    .replaceFirstChar { char -> char.lowercaseChar() }
                return relativeName + "List"
            }

        private const val DOWEL_LIST_PROP_NAME: String = "values"
    }
}
