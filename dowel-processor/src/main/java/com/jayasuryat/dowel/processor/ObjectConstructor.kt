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

import com.jayasuryat.dowel.processor.model.ClassRepresentation
import com.jayasuryat.dowel.processor.model.ClassRepresentation.ParameterSpec.*
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.MemberName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.withIndent
import java.util.*
import kotlin.random.Random

internal class ObjectConstructor {

    fun constructObjectFor(
        representation: ClassRepresentation,
    ): CodeBlock {

        val className = representation.declaration.simpleName.asString()

        val codeBlock = CodeBlock.builder()
            .addStatement("$className(")
            .withIndent {
                representation.parameters
                    .filter { parameter -> !parameter.hasDefault }
                    .forEach { parameter ->
                        val block = buildCodeBlock {
                            add("%L = %L,\n", parameter.name, parameter.spec.getAssigner())
                        }
                        add(block)
                    }
            }.addStatement("),")

        return codeBlock.build()
    }

    private fun ClassRepresentation.ParameterSpec.getAssigner(): CodeBlock {

        val assignment: CodeBlock = when (val spec = this) {

            is IntSpec -> spec.getIntAssigner()
            is LongSpec -> spec.getLongAssigner()
            is FloatSpec -> spec.getFloatAssigner()
            is DoubleSpec -> spec.getDoubleAssigner()
            is CharSpec -> spec.getCharAssigner()
            is BooleanSpec -> spec.getBoolAssigner()
            is StringSpec -> spec.getStringAssigner()

            is StateSpec -> spec.getStateAssigner() // This would be a recursive call
            is ListSpec -> spec.getListAssigner() // This would be a recursive call
            is MapSpec -> spec.getMapAssigner() // This would be a recursive call
            is FlowSpec -> spec.getFlowAssigner() // This would be a recursive call
            is PairSpec -> spec.getPairAssigner() // This would be a recursive call

            is FunctionSpec -> spec.getFunctionAssigner()
            is EnumSpec -> spec.getEnumAssigner()
            is DowelSpec -> spec.getDowelAssigner()
        }

        return assignment
    }

    private fun IntSpec.getIntAssigner(): CodeBlock {

        val value: Int = Random.nextInt(
            from = range.start.toSafeRangeInt(),
            until = range.end.toSafeRangeInt(),
        )

        return buildCodeBlock { add("%L", value) }
    }

    private fun LongSpec.getLongAssigner(): CodeBlock {

        val value: Long = Random.nextLong(
            from = range.start,
            until = range.end,
        )

        return buildCodeBlock { add("%LL", value) }
    }

    private fun FloatSpec.getFloatAssigner(): CodeBlock {

        val start = range.start.toSafeRangeFloat()
        val end = range.end.toSafeRangeFloat()

        val backingValue: Double = start + ((end - start) * Random.nextDouble())
        val value: Float = backingValue.toSafeRangeFloat()

        return buildCodeBlock { add("%Lf", value) }
    }

    private fun DoubleSpec.getDoubleAssigner(): CodeBlock {

        val value: Double = range.start + ((range.end - range.start) * Random.nextDouble())
        return buildCodeBlock { add("%L", value) }
    }

    @Suppress("unused")
    private fun CharSpec.getCharAssigner(): CodeBlock {
        val range = 'a'..'z'
        return buildCodeBlock { add("\'%L\'", range.random()) }
    }

    @Suppress("unused")
    private fun BooleanSpec.getBoolAssigner(): CodeBlock {
        return buildCodeBlock { add("%L", Random.nextBoolean()) }
    }

    private fun StringSpec.getStringAssigner(): CodeBlock {

        val length: Int = if (size.value == -1L) {
            val min = maxOf(size.min.toSafeRangeInt(), 0)
            val max = minOf(size.max.toSafeRangeInt(), MAX_GENERATED_STRING_LENGTH)
            Random.nextInt(from = min, until = max)
        } else size.value.toSafeRangeInt()

        val firstWord = StringSource.getRandomWord()
            .replaceFirstChar { char -> char.titlecase(Locale.getDefault()) }

        val builder = StringBuilder(firstWord)
        while (builder.length < length + 1) {
            builder.append("${StringSource.getRandomWord()} ")
        }

        val value = builder
            .take(length)
            .toString()

        return buildCodeBlock { add("%S", value) }
    }

    private fun StateSpec.getStateAssigner(): CodeBlock {

        val spec = this

        return buildCodeBlock {
            val mutableStateOf = MemberName("androidx.compose.runtime", "mutableStateOf")
            add("%M(%L)", mutableStateOf, spec.elementSpec.getAssigner())
        }
    }

    private fun ListSpec.getListAssigner(): CodeBlock {

        val spec = this

        val listSize = spec.size.value.toSafeRangeInt()

        if (listSize == 0) {
            return buildCodeBlock { add("listOf()") }
        }

        val modListSize: Int = if (listSize != -1) listSize
        else Random.nextLong(
            from = spec.size.min,
            until = spec.size.max,
        ).toSafeRangeInt()

        return buildCodeBlock {
            add("listOf(\n")
            withIndent {
                repeat(modListSize) {
                    add("%L,\n", spec.elementSpec.getAssigner())
                }
            }
            add(")")
        }
    }

    private fun MapSpec.getMapAssigner(): CodeBlock {

        val spec = this

        val listSize = spec.size.value.toSafeRangeInt()

        if (listSize == 0) {
            return buildCodeBlock { add("mapOf()") }
        }

        val modSize: Int = if (listSize != -1) listSize
        else Random.nextLong(
            from = spec.size.min,
            until = spec.size.max,
        ).toSafeRangeInt()

        return buildCodeBlock {
            add("mapOf(\n")
            withIndent {
                repeat(modSize) {
                    add(
                        "%L to %L,\n",
                        spec.keySpec.getAssigner(),
                        spec.valueSpec.getAssigner(),
                    )
                }
            }
            add(")")
        }
    }

    private fun FlowSpec.getFlowAssigner(): CodeBlock {

        val spec = this

        return buildCodeBlock {
            val mutableStateOf = MemberName("kotlinx.coroutines.flow", "flowOf")
            add("%M(%L)", mutableStateOf, spec.elementSpec.getAssigner())
        }
    }

    private fun PairSpec.getPairAssigner(): CodeBlock {

        val spec = this

        return buildCodeBlock {

            add(
                "%T(%L, %L)",
                Pair::class.java,
                spec.leftElementSpec.getAssigner(),
                spec.rightElementSpec.getAssigner(),
            )
        }
    }

    private fun FunctionSpec.getFunctionAssigner(): CodeBlock {

        if (argumentsSize == 0 && isReturnTypeUnit)
            return buildCodeBlock { add("{}") }

        return buildCodeBlock {
            add("{")
            repeat(argumentsSize) { add(" _,") }
            add(" ->")
            if (!isReturnTypeUnit) add(" TODO()")
            add(" }")
        }
    }

    private fun EnumSpec.getEnumAssigner(): CodeBlock {
        return buildCodeBlock {
            add("%L.values().random()", enumDeclaration.simpleName.asString())
        }
    }

    private fun DowelSpec.getDowelAssigner(): CodeBlock {

        val spec = this

        return buildCodeBlock {
            val propName = spec.declaration.dowelListPropertyName
            add("$propName.random()")
        }
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
