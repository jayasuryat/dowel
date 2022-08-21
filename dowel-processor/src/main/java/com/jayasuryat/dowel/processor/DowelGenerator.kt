package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

internal class DowelGenerator(
    private val builtIns: KSBuiltIns,
    private val codeGenerator: CodeGenerator,
    private val logger: KSPLogger,
) {

    private val objectConstructor: ObjectConstructor by lazy {
        ObjectConstructor(
            builtIns = builtIns,
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
            .withIndent {
                addStatement("sequenceOf(")
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
