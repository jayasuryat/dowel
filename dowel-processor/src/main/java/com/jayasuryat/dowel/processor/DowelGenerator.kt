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
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.model.ClassRepresentation
import com.jayasuryat.dowel.processor.model.ClassRepresentationMapper
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.jayasuryat.dowel.processor.util.writeTo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

internal class DowelGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    private val mapper: ClassRepresentationMapper by unsafeLazy {
        ClassRepresentationMapper(
            resolver = resolver,
            logger = logger,
        )
    }
    private val objectConstructor: ObjectConstructor by unsafeLazy { ObjectConstructor() }

    fun generatePreviewParameterProviderFor(
        classDeclaration: KSClassDeclaration,
    ) {

        val fileSpec: FileSpec = FileSpec
            .builder(
                packageName = classDeclaration.packageName.asString(),
                fileName = classDeclaration.dowelClassName,
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

        val outputClassName = classDeclaration.dowelClassName

        // Annotated class's class name
        val declarationClassName = ClassName(
            packageName = packageName,
            classDeclaration.simpleName.asString()
        )

        // Super-type of the generated class
        val outputSuperType = Names.previewParamProvider.parameterizedBy(declarationClassName)

        val representation: ClassRepresentation = mapper.map(classDeclaration)

        val sequenceSize: Int = classDeclaration.annotations
            .first { it.shortName.asString() == Dowel::class.java.simpleName }
            .arguments
            .first { it.name!!.asString() == Dowel.Companion.COUNT_PROPERTY_NAME }
            .value as Int

        val classSpec: TypeSpec = TypeSpec
            .classBuilder(outputClassName)
            .addSuperinterface(outputSuperType)
            .addDowelProperties(representation = representation)
            .addValuesProperty(
                representation = representation,
                objectConstructor = objectConstructor,
                instanceCount = sequenceSize,
            ).build()

        this.addType(typeSpec = classSpec)

        return this
    }

    private fun TypeSpec.Builder.addDowelProperties(
        representation: ClassRepresentation,
    ): TypeSpec.Builder {

        val specs = representation.parameters.map { it.spec }

        val dowelObjects: List<ClassRepresentation.ParameterSpec.DowelSpec> = specs
            .filterIsInstance<ClassRepresentation.ParameterSpec.DowelSpec>()

        val dowelObjectLists: List<ClassRepresentation.ParameterSpec.DowelSpec> = specs
            .filterIsInstance<ClassRepresentation.ParameterSpec.ListSpec>()
            .map { it.elementSpec }
            .filterIsInstance<ClassRepresentation.ParameterSpec.DowelSpec>()

        val allDowelSpecs: List<ClassRepresentation.ParameterSpec.DowelSpec> = buildList {
            addAll(dowelObjects)
            addAll(dowelObjectLists)
        }.distinct()

        val parameters: List<PropertySpec> = allDowelSpecs
            .map { spec ->

                val declaration = spec.declaration

                val declarationType = ClassName(
                    packageName = declaration.packageName.asString(),
                    declaration.simpleName.asString()
                )

                val declarationListType = List::class.asTypeName()
                    .parameterizedBy(declarationType)

                val sequenceProperty = PropertySpec.builder(
                    name = declaration.dowelListPropertyName,
                    type = declarationListType,
                    modifiers = listOf(KModifier.PRIVATE),
                ).initializer("${declaration.dowelClassName}().values.toList()")

                sequenceProperty.build()
            }

        this.addProperties(parameters)

        return this
    }

    private fun TypeSpec.Builder.addValuesProperty(
        representation: ClassRepresentation,
        objectConstructor: ObjectConstructor,
        instanceCount: Int,
    ): TypeSpec.Builder {

        val declaration = representation.declaration

        // Annotated class's class name
        val declarationClassName = ClassName(
            packageName = declaration.packageName.asString(),
            declaration.simpleName.asString()
        )

        val propertyType = Names.sequenceName.parameterizedBy(declarationClassName)

        val initializer: CodeBlock = CodeBlock.builder()
            .addStatement("sequenceOf(")
            .withIndent {
                repeat(instanceCount) {
                    val constructed: CodeBlock =
                        objectConstructor.constructObjectFor(representation)
                    add(constructed)
                }
            }
            .addStatement(")")
            .build()

        val valuesProperty: PropertySpec = PropertySpec.builder(
            name = DOWEL_PROP_NAME,
            type = propertyType,
            modifiers = listOf(KModifier.OVERRIDE),
        ).initializer(initializer)
            .build()

        this.addProperty(valuesProperty)

        return this
    }

    companion object {

        private const val DOWEL_PROP_NAME: String = "values"
    }
}
