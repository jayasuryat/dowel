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
package com.jayasuryat.dowel.processor.model

import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.DefaultRange
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.annotation.FloatRange
import com.jayasuryat.dowel.processor.annotation.IntRange
import com.jayasuryat.dowel.processor.annotation.Size
import com.jayasuryat.dowel.processor.model.ClassRepresentation.ParameterSpec.*
import com.jayasuryat.dowel.processor.util.logError
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.squareup.kotlinpoet.ksp.toTypeName

// TODO: Update mapping logic from IntRange, FloatRange, and Size. Can filter out default values here.

internal class ClassRepresentationMapper(
    private val resolver: Resolver,
    private val logger: KSPLogger,
) {

    private val builtIns: KSBuiltIns = resolver.builtIns

    private val listDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(List::class.qualifiedName!!)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val mapDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Map::class.qualifiedName!!)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val stateDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.stateName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val flowDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.flowName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val pairDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Pair::class.qualifiedName!!)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }

    fun map(
        classDeclaration: KSClassDeclaration,
    ): ClassRepresentation {

        val constructor: KSFunctionDeclaration = classDeclaration.primaryConstructor!!
        val parameters: List<KSValueParameter> = constructor.parameters

        val mappedParameters: List<ClassRepresentation.Parameter> = parameters.map { parameter ->

            val resolvedType = parameter.type.resolve()
            val spec: ClassRepresentation.ParameterSpec = resolvedType.getSpec(
                annotations = parameter.annotations.toList(),
            )

            parameter.mapToParameter(
                spec = spec,
            )
        }

        return ClassRepresentation(
            declaration = classDeclaration,
            parameters = mappedParameters,
        )
    }

    private fun KSType.getSpec(
        annotations: List<KSAnnotation>,
    ): ClassRepresentation.ParameterSpec {

        val propType: KSType = this
        val propTypeDeclaration: KSDeclaration = propType.declaration

        val paramSpec: ClassRepresentation.ParameterSpec = when {

            // Primitives
            propType.isAssignableFrom(builtIns.intType) -> getIntSpec(annotations)
            propType.isAssignableFrom(builtIns.longType) -> getLongSpec(annotations)
            propType.isAssignableFrom(builtIns.floatType) -> getFloatSpec(annotations)
            propType.isAssignableFrom(builtIns.doubleType) -> getDoubleSpec(annotations)
            propType.isAssignableFrom(builtIns.charType) -> getCharSpec()
            propType.isAssignableFrom(builtIns.booleanType) -> getBooleanSpec()
            propType.isAssignableFrom(builtIns.stringType) -> getStringSpec(annotations)

            // State
            stateDeclaration.isAssignableFrom(propType) -> propType.getStateSpec()

            // List
            listDeclaration.isAssignableFrom(propType) -> propType.getListSpec(annotations)

            // Map
            mapDeclaration.isAssignableFrom(propType) -> propType.getMapSpec(annotations)

            // Flow
            flowDeclaration.isAssignableFrom(propType) -> propType.getFlowSpec()

            // Pair
            pairDeclaration.isAssignableFrom(propType) -> propType.getPairSpec()

            // High-order functions
            propType.isFunctionType || propType.isSuspendFunctionType -> propType.getFunctionSpec()

            // Enum classes
            propTypeDeclaration is KSClassDeclaration &&
                propTypeDeclaration.classKind == ClassKind.ENUM_CLASS -> propTypeDeclaration.getEnumSpec()

            // Class annotated with @Dowel annotation
            propTypeDeclaration.isDowelClass() -> propTypeDeclaration.getDowelSpec()

            else -> {
                // TODO: Fix message
                logger.logError(
                    message =
                    "Dowel does not support generating preview param providers for the type ${propType.toTypeName()} (${propTypeDeclaration.simpleName.asString()}.${propTypeDeclaration.simpleName.asString()}).",
                    node = propTypeDeclaration,
                )
            }
        }

        return paramSpec
    }

    private fun KSValueParameter.mapToParameter(
        spec: ClassRepresentation.ParameterSpec,
    ): ClassRepresentation.Parameter {

        val prop = this

        return ClassRepresentation.Parameter(
            spec = spec,
            name = prop.name!!.asString(),
            hasDefault = prop.hasDefault,
        )
    }

    private fun getIntSpec(
        annotations: List<KSAnnotation>,
    ): IntSpec {

        val range: IntRange = IntRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_INT_MIN,
            defaultEnd = DefaultRange.DEFAULT_INT_MAX,
        )

        return IntSpec(
            range = range,
        )
    }

    private fun getLongSpec(
        annotations: List<KSAnnotation>,
    ): LongSpec {

        val range: IntRange = IntRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_LONG_MIN,
            defaultEnd = DefaultRange.DEFAULT_LONG_MAX,
        )

        return LongSpec(
            range = range
        )
    }

    private fun getFloatSpec(
        annotations: List<KSAnnotation>,
    ): FloatSpec {

        val range: FloatRange = FloatRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_FLOAT_MIN,
            defaultEnd = DefaultRange.DEFAULT_FLOAT_MAX,
        )

        return FloatSpec(
            range = range,
        )
    }

    private fun getDoubleSpec(
        annotations: List<KSAnnotation>,
    ): DoubleSpec {

        val range: FloatRange = FloatRange.find(
            annotations = annotations.toList(),
            defaultStart = DefaultRange.DEFAULT_DOUBLE_MIN,
            defaultEnd = DefaultRange.DEFAULT_DOUBLE_MAX,
        )

        return DoubleSpec(
            range = range,
        )
    }

    private fun getBooleanSpec(): BooleanSpec = BooleanSpec

    private fun getCharSpec(): CharSpec = CharSpec

    private fun getStringSpec(
        annotations: List<KSAnnotation>,
    ): StringSpec {

        val size: Size = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_STRING_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_STRING_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_STRING_LEN_MAX,
        )

        return StringSpec(
            size = size,
        )
    }

    private fun KSType.getStateSpec(): StateSpec {

        val arg = this.arguments.first()

        val resolvedType = arg.type!!.resolve()
        val spec: ClassRepresentation.ParameterSpec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return StateSpec(
            elementSpec = spec
        )
    }

    private fun KSType.getListSpec(
        annotations: List<KSAnnotation>,
    ): ListSpec {

        val size: Size = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_LIST_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_LIST_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_LIST_LEN_MAX,
        )

        require(this.arguments.size == 1) { "List should have only one type argument. Current size = ${this.arguments.size}" }

        val arg = this.arguments.first()

        val resolvedType = arg.type!!.resolve()
        val spec: ClassRepresentation.ParameterSpec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return ListSpec(
            size = size,
            elementSpec = spec,
        )
    }

    private fun KSType.getMapSpec(
        annotations: List<KSAnnotation>,
    ): MapSpec {

        fun KSTypeArgument.getSpec(): ClassRepresentation.ParameterSpec {
            val resolvedType = this.type!!.resolve()
            return resolvedType.getSpec(
                annotations = this.annotations.toList(),
            )
        }

        val size: Size = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_MAP_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_MAP_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_MAP_LEN_MAX,
        )

        val key = this.arguments[0].getSpec()
        val value = this.arguments[1].getSpec()

        return MapSpec(
            size = size,
            keySpec = key,
            valueSpec = value,
        )
    }

    private fun KSType.getFlowSpec(): FlowSpec {

        val arg = this.arguments.first()

        val resolvedType = arg.type!!.resolve()
        val spec: ClassRepresentation.ParameterSpec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return FlowSpec(
            elementSpec = spec
        )
    }

    private fun KSType.getPairSpec(): PairSpec {

        fun KSTypeArgument.getSpec(): ClassRepresentation.ParameterSpec {
            val resolvedType = this.type!!.resolve()
            return resolvedType.getSpec(
                annotations = this.annotations.toList(),
            )
        }

        val left = this.arguments[0].getSpec()
        val right = this.arguments[1].getSpec()

        return PairSpec(
            leftElementSpec = left,
            rightElementSpec = right,
        )
    }

    private fun KSType.getFunctionSpec(): FunctionSpec {

        val ksType = this

        val returnType: KSType = ksType.arguments.last().type!!.resolve()
        val isReturnTypeUnit: Boolean = returnType.isAssignableFrom(resolver.builtIns.unitType)

        return FunctionSpec(
            argumentsSize = ksType.arguments.size - 1,
            isReturnTypeUnit = isReturnTypeUnit
        )
    }

    private fun KSClassDeclaration.getEnumSpec(): EnumSpec {

        return EnumSpec(
            enumDeclaration = this
        )
    }

    private fun KSDeclaration.getDowelSpec(): DowelSpec {

        val declaration = this as KSClassDeclaration

        return DowelSpec(
            declaration = declaration,
        )
    }

    private fun KSDeclaration.isDowelClass(): Boolean {
        val declaration = this as? KSClassDeclaration ?: return false
        val dowelName = Dowel::class.java.simpleName
        val annotation = declaration.annotations.find { it.shortName.asString() == dowelName }
        return annotation != null
    }
}
