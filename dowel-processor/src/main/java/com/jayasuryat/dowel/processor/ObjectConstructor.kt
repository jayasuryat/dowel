package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent
import kotlin.random.Random

internal class ObjectConstructor(
    private val builtIns: KSBuiltIns,
    private val logger: KSPLogger,
) {

    fun constructObjectFor(
        classDeclaration: KSClassDeclaration,
    ): CodeBlock {

        val constructor: KSFunctionDeclaration = classDeclaration.primaryConstructor!!

        val codeBlock = CodeBlock.builder()
            .addStatement("${classDeclaration.simpleName.asString()}(")
            .withIndent {

                constructor.parameters.forEach { parameter ->
                    addStatement("${parameter.assignment(classDeclaration = classDeclaration)},")
                }

            }.addStatement("),")

        return codeBlock.build()
    }

    private fun KSValueParameter.assignment(
        classDeclaration: KSClassDeclaration,
    ): String {

        val prop = this
        val type = prop.type.resolve()

        val value: String = when {
            type.isAssignableFrom(builtIns.intType) -> "${Random.nextInt()}"
            type.isAssignableFrom(builtIns.longType) -> "${Random.nextLong()}"
            type.isAssignableFrom(builtIns.floatType) -> "${Random.nextFloat() * Random.nextInt()}"
            type.isAssignableFrom(builtIns.doubleType) -> "${Random.nextDouble()}"
            type.isAssignableFrom(builtIns.booleanType) -> "${Random.nextBoolean()}"
            type.isAssignableFrom(builtIns.charType) -> "\'y\'"
            type.isAssignableFrom(builtIns.stringType) -> "\"Hello there!\""
            else -> {
                logger.error(
                    message = "Dowel does not support generating preview param providers for the type ${type.toTypeName()}",
                    symbol = classDeclaration,
                )
                ""
            }
        }

        return "${prop.name!!.asString()} = $value"
    }
}