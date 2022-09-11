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

/**
 * Mapper class to map a [KSClassDeclaration] to [ClassRepresentation]. This mapper only considers the
 * properties listed in the primary constructor of the [KSClassDeclaration] and in that ignores all
 * of the properties which have default values.
 *
 * **Note** : This mapper will call [KSTypeReference.resolve] method on every property, which is
 * an expensive call. It is suggested to map a [KSClassDeclaration] once and reuse the resultant
 * [ClassRepresentation] instead of mapping multiple times.
 *
 * @param resolver to resolve types of each property
 * @param logger to log errors when unexpected or invalid inputs are encountered
 *
 * @see [ClassRepresentation]
 * @see [KSTypeReference.resolve]
 */
internal class ClassRepresentationMapper(
    private val resolver: Resolver,
    private val logger: KSPLogger,
    private val predefinedProviders: UserPredefinedParamProviders,
) {

    // region : Types
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
    // endregion

    fun map(
        classDeclaration: KSClassDeclaration,
    ): ClassRepresentation {

        val constructor: KSFunctionDeclaration = classDeclaration.primaryConstructor!!
        val parameters: List<KSValueParameter> = constructor.parameters

        val mappedParameters: List<ClassRepresentation.Parameter> = parameters
            .filter { !it.hasDefault } // Ignoring all properties with default values
            .map { parameter ->

                val resolvedType = parameter.type.resolve()
                val spec: ClassRepresentation.ParameterSpec = resolvedType.getSpec(
                    annotations = parameter.annotations.toList(),
                )

                parameter.mapToParameter(
                    spec = spec,
                    type = resolvedType,
                )
            }

        return ClassRepresentation(
            declaration = classDeclaration,
            parameters = mappedParameters,
        )
    }

    /**
     * Maps give [KSType] to an appropriate concrete representation i.e.,
     * [ClassRepresentation.ParameterSpec].
     */
    private fun KSType.getSpec(
        annotations: List<KSAnnotation>,
    ): ClassRepresentation.ParameterSpec {

        val propType: KSType = this
        val propTypeDeclaration: KSDeclaration = propType.declaration

        // Ordering is crucial here. Only checks for the first matching type
        val paramSpec: ClassRepresentation.ParameterSpec = when {

            // User has pre-defined a PreviewParameterProvider for this type
            // As this check is done first, this spec takes precedence over all the other specs
            // which may also match.
            predefinedProviders[propType.makeNotNullable()] != null -> propType.getPreDefinedProviderSpec()

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

            // Unsupported types which are nullable
            propType.isMarkedNullable -> getUnsupportedNullableSpec()

            else -> {

                val propName = propTypeDeclaration.parentDeclaration!!.simpleName.asString() + "." +
                    propTypeDeclaration.simpleName.asString()

                logger.logError(
                    message = "Dowel does not support generating preview param providers for the type " +
                        "${propType.toTypeName()} @ ($propName).",
                    node = propTypeDeclaration,
                )
            }
        }

        return paramSpec
    }

    private fun KSValueParameter.mapToParameter(
        spec: ClassRepresentation.ParameterSpec,
        type: KSType,
    ): ClassRepresentation.Parameter {

        val prop = this

        return ClassRepresentation.Parameter(
            spec = spec,
            name = prop.name!!.asString(),
            isNullable = type.nullability == Nullability.NULLABLE,
        )
    }

    private fun KSType.getPreDefinedProviderSpec(): PreDefinedProviderSpec {

        val declaration: KSClassDeclaration? = predefinedProviders[this.makeNotNullable()]
        requireNotNull(declaration) { "Dowel internal error, something went wrong while processing predefined param providers for $this" }

        return PreDefinedProviderSpec(
            provider = declaration,
            type = this,
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

        require(this.arguments.size == 1) { "State must have have exactly one type argument. Current size = ${this.arguments.size}" }

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

        require(this.arguments.size == 1) { "List must have have exactly one type argument. Current size = ${this.arguments.size}" }

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

        require(this.arguments.size == 2) { "Map must have have exactly two type arguments. Current size = ${this.arguments.size}" }

        val key = this.arguments[0].getSpec()
        val value = this.arguments[1].getSpec()

        return MapSpec(
            size = size,
            keySpec = key,
            valueSpec = value,
        )
    }

    private fun KSType.getFlowSpec(): FlowSpec {

        require(this.arguments.size == 1) { "Flow must have have exactly one type argument. Current size = ${this.arguments.size}" }

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

        require(this.arguments.size == 2) { "Pair must have have exactly two type arguments. Current size = ${this.arguments.size}" }

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
            type = declaration.asType(listOf()), // TODO: Revisit this
        )
    }

    private fun getUnsupportedNullableSpec(): UnsupportedNullableSpec = UnsupportedNullableSpec

    private fun KSDeclaration.isDowelClass(): Boolean {
        val declaration = this as? KSClassDeclaration ?: return false
        val dowelName = Dowel::class.java.simpleName
        val annotation = declaration.annotations.find { it.shortName.asString() == dowelName }
        return annotation != null
    }
}
