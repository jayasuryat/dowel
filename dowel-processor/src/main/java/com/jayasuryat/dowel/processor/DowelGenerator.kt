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
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal class DowelGenerator(
    private val resolver: Resolver,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    private val objectConstructor: ObjectConstructor by lazy {
        ObjectConstructor(
            resolver = resolver,
            logger = logger,
        )
    }

    fun generatePreviewParameterProviderFor(
        classDeclaration: KSClassDeclaration,
    ) {

        val outputPackageName = classDeclaration.packageName.asString()
        val outputName = "${classDeclaration.simpleName.asString()}PreviewParamProvider"

        val fileSpec: FileSpec = FileSpec
            .builder(
                packageName = outputPackageName,
                fileName = outputName
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

        val outputClassName = "${classDeclaration.simpleName.asString()}PreviewParamProvider"

        // Annotated class's class name
        val declarationClassName = ClassName(
            packageName = packageName,
            classDeclaration.simpleName.asString()
        )

        // Super-type of the generated class
        val outputSuperType = ClassNames.previewParamProvider.parameterizedBy(declarationClassName)

        val propertyType = ClassName(
            packageName = "kotlin.sequences",
            "Sequence",
        ).parameterizedBy(declarationClassName)

        val sequenceProperty: PropertySpec = PropertySpec.builder(
            name = "values",
            type = propertyType,
            modifiers = listOf(KModifier.OVERRIDE),
        ).initializer(classDeclaration.sequenceOfInitializations(instanceCount = 5))
            .build()

        val classSpec: TypeSpec = TypeSpec
            .classBuilder(outputClassName)
            .addSuperinterface(outputSuperType)
            .addProperty(sequenceProperty)
            .build()

        this.addType(typeSpec = classSpec)

        return this
    }

    private fun KSClassDeclaration.sequenceOfInitializations(
        instanceCount: Int,
    ): CodeBlock {

        val classDeclaration = this

        val propertyInitializer = CodeBlock.builder()
            .addStatement("sequenceOf(")
            .withIndent {
                repeat(instanceCount) {
                    val constructed = objectConstructor.constructObjectFor(classDeclaration)
                    add(constructed)
                }
            }
            .addStatement(")")
            .build()

        return propertyInitializer
    }

    private fun FileSpec.writeTo(
        codeGenerator: CodeGenerator,
        dependencies: Dependencies = Dependencies(false),
    ) {
        val file = codeGenerator.createNewFile(dependencies, packageName, name)
        OutputStreamWriter(file, StandardCharsets.UTF_8).use(::writeTo)
    }
}
