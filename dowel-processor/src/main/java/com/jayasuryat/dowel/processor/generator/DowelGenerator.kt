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
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.dowelClassName
import com.jayasuryat.dowel.processor.dowelListPropertyName
import com.jayasuryat.dowel.processor.model.ClassRepresentation
import com.jayasuryat.dowel.processor.model.ClassRepresentation.ParameterSpec.DowelSpec
import com.jayasuryat.dowel.processor.model.ClassRepresentationMapper
import com.jayasuryat.dowel.processor.util.asClassName
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.jayasuryat.dowel.processor.util.writeTo
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

/**
 * Generates a file containing an implementation of
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] for a [KSClassDeclaration].
 *
 * The generated file would have the same package as the [KSClassDeclaration]
 * but would be in the generated sources. File name would be <Name of [KSClassDeclaration]>PreviewParamProvider.kt
 *
 * @see [DowelListGenerator]
 */
@Suppress("KDocUnresolvedReference")
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

    /**
     * Generates an implementation of [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
     * for [classDeclaration]
     */
    fun generatePreviewParameterProviderFor(
        classDeclaration: KSClassDeclaration,
    ) {

        val fileSpec: FileSpec = FileSpec
            .builder(
                packageName = classDeclaration.packageName.asString(),
                fileName = classDeclaration.dowelClassName,
            ).addPreviewParamProvider(
                classDeclaration = classDeclaration,
                objectConstructor = objectConstructor,
            ).build()

        // Creating a file with specified specs and flushing code into it
        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(
                aggregating = true,
                classDeclaration.containingFile!!
            )
        )
    }

    /**
     * Method responsible for generating all of the code inside the implementation of
     * [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
     */
    private fun FileSpec.Builder.addPreviewParamProvider(
        classDeclaration: KSClassDeclaration,
        objectConstructor: ObjectConstructor,
    ): FileSpec.Builder {

        val outputClassName = classDeclaration.dowelClassName

        // Annotated class's class name
        val declarationClassName = classDeclaration.asClassName()

        // Super-type of the generated class
        val outputSuperType = Names.previewParamProvider.parameterizedBy(declarationClassName)

        // KSClassDeclaration mapped to an intermediate representation to help with code generation
        val representation: ClassRepresentation = mapper.map(classDeclaration)

        // Number of objects to be generated as part of the "values" sequence property
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

    /**
     * Adds private properties inside the generated class which help in simplifying code generation.
     *
     * Dowel supports nesting of Dowel classes (read more at [Dowel]), in such cases code is need to
     * to be generated for the nested objects as well. Instead of generating code for every nested
     * Dowel class by *hand*, their respective (generated) PreviewParameterProviders are reused to
     * provide instances of those respective types.
     *
     * This method adds instances of such PreviewParameterProviders as private properties inside the
     * generated class.
     */
    private fun TypeSpec.Builder.addDowelProperties(
        representation: ClassRepresentation,
    ): TypeSpec.Builder {

        // All the dowel specs in the representation
        val allDowelSpecs = representation.getAllDowelSpecsRecursively()

        // List of all the properties that are needed to be added to the generated class
        val properties: List<PropertySpec> = allDowelSpecs
            .map { spec ->

                val declaration = spec.declaration

                val declarationType = declaration.asClassName()
                val declarationListType = List::class.asTypeName()
                    .parameterizedBy(declarationType)

                val sequenceProperty = PropertySpec.builder(
                    name = declaration.dowelListPropertyName,
                    type = declarationListType,
                    modifiers = listOf(KModifier.PRIVATE),
                ).initializer("${declaration.dowelClassName}().values.toList()")

                sequenceProperty.build()
            }

        this.addProperties(properties)

        return this
    }

    /**
     * Adds the overridden "values" property of the [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
     * class with a [Sequence] of values of type represented by [representation]. This method
     * generates code for instantiation logic of all the properties listed in the [representation].
     */
    private fun TypeSpec.Builder.addValuesProperty(
        representation: ClassRepresentation,
        objectConstructor: ObjectConstructor,
        instanceCount: Int,
    ): TypeSpec.Builder {

        // Annotated class's class name
        val declarationClassName = representation.declaration.asClassName()

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

    /**
     * Finds list of all the [DowelSpec] in a given [ClassRepresentation] by
     * recursively searching in all of the type parameters of all properties.
     */
    private fun ClassRepresentation.getAllDowelSpecsRecursively(): List<DowelSpec> {

        /**
         * Finds all the [DowelSpec] in a specific [ClassRepresentation.ParameterSpec] by
         * recursively searching in all of the type parameters.
         */
        fun ClassRepresentation.ParameterSpec.getAllDowelSpecsRecursively(): List<DowelSpec> {

            val specs: List<DowelSpec> = when (val spec = this) {

                is ClassRepresentation.ParameterSpec.IntSpec,
                is ClassRepresentation.ParameterSpec.LongSpec,
                is ClassRepresentation.ParameterSpec.FloatSpec,
                is ClassRepresentation.ParameterSpec.DoubleSpec,
                is ClassRepresentation.ParameterSpec.CharSpec,
                is ClassRepresentation.ParameterSpec.BooleanSpec,
                is ClassRepresentation.ParameterSpec.StringSpec,
                is ClassRepresentation.ParameterSpec.FunctionSpec,
                is ClassRepresentation.ParameterSpec.EnumSpec,
                is ClassRepresentation.ParameterSpec.UnsupportedNullableSpec,
                -> emptyList()

                is ClassRepresentation.ParameterSpec.StateSpec ->
                    spec.elementSpec.getAllDowelSpecsRecursively()

                is ClassRepresentation.ParameterSpec.ListSpec ->
                    spec.elementSpec.getAllDowelSpecsRecursively()

                is ClassRepresentation.ParameterSpec.MapSpec ->
                    spec.keySpec.getAllDowelSpecsRecursively() +
                            spec.valueSpec.getAllDowelSpecsRecursively()

                is ClassRepresentation.ParameterSpec.FlowSpec ->
                    spec.elementSpec.getAllDowelSpecsRecursively()

                is ClassRepresentation.ParameterSpec.PairSpec ->
                    spec.leftElementSpec.getAllDowelSpecsRecursively() +
                            spec.rightElementSpec.getAllDowelSpecsRecursively()

                is DowelSpec -> listOf(spec)
            }

            return specs
        }

        return parameters
            .map { parameter -> parameter.spec.getAllDowelSpecsRecursively() }
            .flatten()
            .distinct()
    }

    companion object {

        private const val DOWEL_PROP_NAME: String = "values"
    }
}
