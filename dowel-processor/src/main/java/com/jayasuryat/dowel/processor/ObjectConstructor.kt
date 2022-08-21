package com.jayasuryat.dowel.processor

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.jayasuryat.dowel.processor.annotation.FloatRange
import com.jayasuryat.dowel.processor.annotation.IntRange
import com.jayasuryat.dowel.processor.annotation.Size
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.withIndent
import java.util.*
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
            type.isAssignableFrom(builtIns.intType) -> prop.getIntAssigner()
            type.isAssignableFrom(builtIns.longType) -> prop.getLongAssigner()
            type.isAssignableFrom(builtIns.floatType) -> prop.getFloatAssigner()
            type.isAssignableFrom(builtIns.doubleType) -> prop.getDoubleAssigner()
            type.isAssignableFrom(builtIns.booleanType) -> prop.getBoolAssigner()
            type.isAssignableFrom(builtIns.charType) -> prop.getCharAssigner()
            type.isAssignableFrom(builtIns.stringType) -> prop.getStringAssigner()
            else -> {
                logger.error(
                    message = "Dowel does not support generating preview param providers for the type ${type.toTypeName()} (${classDeclaration.simpleName.asString()}.${this.name!!.asString()}).",
                    symbol = classDeclaration,
                )
                ""
            }
        }

        return "${prop.name!!.asString()} = $value"
    }

    private fun KSValueParameter.getIntAssigner(): String {

        val limit = IntRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_INT_MIN,
            defaultEnd = DefaultRange.DEFAULT_INT_MAX,
        )

        val value: Int = Random.nextInt(
            from = limit.start.toSafeRangeInt(),
            until = limit.end.toSafeRangeInt(),
        )

        return "$value"
    }

    private fun KSValueParameter.getLongAssigner(): String {

        val limit = IntRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_LONG_MIN,
            defaultEnd = DefaultRange.DEFAULT_LONG_MAX,
        )

        val value: Long = Random.nextLong(
            from = limit.start,
            until = limit.end,
        )

        return "$value"
    }

    private fun KSValueParameter.getFloatAssigner(): String {

        val limit = FloatRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_FLOAT_MIN,
            defaultEnd = DefaultRange.DEFAULT_FLOAT_MAX,
        )

        val start = limit.start.toSafeRangeFloat()
        val end = limit.end.toSafeRangeFloat()

        val backingValue: Double = start + ((end - start) * Random.nextDouble())
        val value: Float = backingValue.toSafeRangeFloat()

        return "${value}f"
    }

    private fun KSValueParameter.getDoubleAssigner(): String {

        val limit = FloatRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_DOUBLE_MIN,
            defaultEnd = DefaultRange.DEFAULT_DOUBLE_MAX,
        )

        val value: Double = limit.start + ((limit.end - limit.start) * Random.nextDouble())
        return "$value"
    }

    private fun KSValueParameter.getBoolAssigner(): String {
        return "${Random.nextBoolean()}"
    }

    private fun KSValueParameter.getCharAssigner(): String {
        val range = 'a'..'z'
        return "\'${range.random()}\'"
    }

    private fun KSValueParameter.getStringAssigner(): String {

        val limit = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_STRING_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_STRING_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_STRING_LEN_MAX,
        )

        val length: Int = if (limit.value == -1L) {
            val min = maxOf(limit.min.toSafeRangeInt(), 0)
            val max = minOf(limit.max.toSafeRangeInt(), MAX_GENERATED_STRING_LENGTH)
            Random.nextInt(from = min, until = max)
        } else limit.value.toSafeRangeInt()

        val firstWord = StringSource.getRandomWord()
            .replaceFirstChar { char -> char.titlecase(Locale.getDefault()) }

        val builder = StringBuilder(firstWord)
        while (builder.length < length + 1) {
            builder.append("${StringSource.getRandomWord()} ")
        }

        val value = builder
            .take(length)
            .toString()

        return if (value.length < 30) "\"$value\"" else "\"\"\"$value\"\"\""
    }

    private fun Long.toSafeRangeInt(): Int {
        return if (this < Int.MIN_VALUE) Int.MIN_VALUE
        else if (this > Int.MAX_VALUE) Int.MAX_VALUE
        else this.toInt()
    }

    private fun Double.toSafeRangeFloat(): Float {
        return if (this < Float.MIN_VALUE) Float.MIN_VALUE
        else if (this > Float.MAX_VALUE) Float.MAX_VALUE
        else this.toFloat()
    }

    private companion object {

        private const val MAX_GENERATED_STRING_LENGTH: Int = 600
    }
}