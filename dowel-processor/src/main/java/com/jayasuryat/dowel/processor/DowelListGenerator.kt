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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.processor.util.asClassName
import com.jayasuryat.dowel.processor.util.writeTo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class DowelListGenerator(
    private val codeGenerator: CodeGenerator,
) {

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

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                classDeclaration.containingFile!!
            )
        )
    }

    private fun FileSpec.Builder.addPreviewParamProvider(
        classDeclaration: KSClassDeclaration,
    ): FileSpec.Builder {

        val outputClassName = classDeclaration.dowelListClassName

        // Annotated class's class name
        val declarationClassName = ClassName(
            packageName = packageName,
            classDeclaration.simpleName.asString()
        )

        val listType = Names.listName.parameterizedBy(declarationClassName)

        // Super-type of the generated class
        val outputSuperType = Names.previewParamProvider.parameterizedBy(listType)

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

    private fun TypeSpec.Builder.addDowelListProperty(
        declaration: KSClassDeclaration,
    ): TypeSpec.Builder {

        val declarationName = declaration.asClassName()
        val declarationListType = Names.listName.parameterizedBy(declarationName)

        val values = PropertySpec.builder(
            name = declaration.dowelListPropertyName,
            type = declarationListType,
            modifiers = listOf(KModifier.PRIVATE),
        ).initializer("${declaration.dowelClassName}().values.toList()")
            .build()

        this.addProperty(values)

        return this
    }

    private fun TypeSpec.Builder.addValuesProperty(
        declaration: KSClassDeclaration,
        instanceCount: Int,
    ): TypeSpec.Builder {

        val declarationName = declaration.asClassName()
        val listType = Names.listName.parameterizedBy(declarationName)

        val propertyType = Names.sequenceName.parameterizedBy(listType)

        val randomMember = MemberName(
            packageName = "kotlin.random",
            simpleName = "Random"
        )

        val initializer: CodeBlock = CodeBlock.builder()
            .addStatement("sequence {")
            .withIndent {
                addStatement("repeat(%L) {", instanceCount)
                withIndent {
                    add(
                        "yield(%L.shuffled().take(%M.nextInt(%L.size)))\n",
                        declaration.dowelListPropertyName,
                        randomMember,
                        declaration.dowelListPropertyName,
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

        private const val DOWEL_LIST_PROP_NAME: String = "values"
    }
}
