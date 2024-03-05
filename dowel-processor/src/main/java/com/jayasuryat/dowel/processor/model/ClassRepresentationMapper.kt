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

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.processing.KSBuiltIns
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.*
import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.processor.DefaultRange
import com.jayasuryat.dowel.processor.Names
import com.jayasuryat.dowel.processor.annotation.FloatRange
import com.jayasuryat.dowel.processor.annotation.IntRange
import com.jayasuryat.dowel.processor.annotation.Size
import com.jayasuryat.dowel.processor.dowelClassName
import com.jayasuryat.dowel.processor.model.ClassRepresentation.ParameterSpec
import com.jayasuryat.dowel.processor.model.ClassRepresentation.ParameterSpec.*
import com.jayasuryat.dowel.processor.relativeClassName
import com.jayasuryat.dowel.processor.util.unsafeLazy
import com.jayasuryat.either.Either
import com.jayasuryat.either.either
import com.jayasuryat.either.left
import com.jayasuryat.either.right
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName

/**
 * Types which are completely unsupported by Dowel, and would be logged as an error at some
 * point after getting mapped
 */
private object UnsupportedType

private typealias MaybeParamSpec = Either<UnsupportedType, ParameterSpec>
private typealias MaybeSpec<T> = Either<UnsupportedType, T>

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
        val ksName = resolver.getKSNameFromString(Names.mutableListName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val setDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableSetName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val mapDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableMapName.canonicalName)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val mutableStateFlowNameDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.mutableStateFlowName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.unitType
    }
    private val pairDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Pair::class.qualifiedName!!)
        resolver.getClassDeclarationByName(ksName)!!.asStarProjectedType()
    }
    private val stateDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.stateName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.nothingType
    }
    private val colorDeclaration: KSType by unsafeLazy {
        val ksName = resolver.getKSNameFromString(Names.colorName.canonicalName)
        resolver.getClassDeclarationByName(ksName)?.asStarProjectedType() ?: builtIns.nothingType
    }
    // endregion

    fun map(
        classDeclaration: KSClassDeclaration,
    ): ClassRepresentation {

        val constructor: KSFunctionDeclaration = classDeclaration.primaryConstructor!!
        val parameters: List<KSValueParameter> = constructor.parameters

        val mappedParameters: List<ClassRepresentation.Parameter> = parameters
            .filter { !it.hasDefault } // Ignoring all properties with default values
            .mapNotNull { parameter -> // Filtering out UnsupportedType parameters

                val resolvedType = parameter.type.resolve()
                val spec: MaybeSpec<ParameterSpec> = resolvedType.getSpec(
                    annotations = parameter.annotations.toList(),
                )

                val mappedParam = when (spec) {
                    is Either.Left -> {
                        logger.logUnsupportedTypeError(
                            parentClass = classDeclaration,
                            parameter = parameter,
                            type = resolvedType,
                        )
                        // Mapping UnsupportedType as null and filtering it out
                        null
                    }
                    is Either.Right -> {
                        // Mapping to ClassRepresentation.Parameter type
                        parameter.mapToParameter(
                            spec = spec.value,
                            type = resolvedType,
                        )
                    }
                }
                mappedParam
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
    ): MaybeParamSpec {

        val propType: KSType = this
        val starProjectedType by lazy { propType.starProjection() }
        val propTypeDeclaration: KSDeclaration = propType.declaration

        // Ordering is crucial here. Only checks for the first matching type
        val paramSpec: MaybeParamSpec = when {

            // Defensive checks against the `Nothing` type
            builtIns.nothingType.isAssignableFrom(propType) -> UnsupportedType.left()

            // User has pre-defined a PreviewParameterProvider for this type
            // As this check is done first, this spec takes precedence over all the other specs
            // which may also match.
            predefinedProviders[propType.makeNotNullable()] != null ->
                propType.getPreDefinedProviderSpec().right()

            // Primitives
            propType.isAssignableFrom(builtIns.intType) -> getIntSpec(annotations).right()
            propType.isAssignableFrom(builtIns.longType) -> getLongSpec(annotations).right()
            propType.isAssignableFrom(builtIns.floatType) -> getFloatSpec(annotations).right()
            propType.isAssignableFrom(builtIns.doubleType) -> getDoubleSpec(annotations).right()
            propType.isAssignableFrom(builtIns.charType) -> getCharSpec().right()
            propType.isAssignableFrom(builtIns.booleanType) -> getBooleanSpec().right()
            propType.isAssignableFrom(builtIns.stringType) -> getStringSpec(annotations).right()

            // List
            starProjectedType.isAssignableFrom(listDeclaration) -> propType.getListSpec(annotations)

            // Set
            starProjectedType.isAssignableFrom(setDeclaration) -> propType.getSetSpec(annotations)

            // Map
            starProjectedType.isAssignableFrom(mapDeclaration) -> propType.getMapSpec(annotations)

            // Flow
            starProjectedType.isAssignableFrom(mutableStateFlowNameDeclaration) -> propType.getFlowSpec()

            // Pair
            pairDeclaration.isAssignableFrom(propType) -> propType.getPairSpec()

            // High-order functions
            propType.isFunctionType || propType.isSuspendFunctionType ->
                propType.getFunctionSpec().right()

            // Sealed class
            propTypeDeclaration is KSClassDeclaration &&
                propTypeDeclaration.modifiers.contains(Modifier.SEALED) ->
                propTypeDeclaration.getSealedSpec()

            // Enum classes
            propTypeDeclaration is KSClassDeclaration &&
                propTypeDeclaration.classKind == ClassKind.ENUM_CLASS ->
                propTypeDeclaration.getEnumSpec().right()

            // Object
            propTypeDeclaration is KSClassDeclaration &&
                propTypeDeclaration.classKind == ClassKind.OBJECT ->
                propTypeDeclaration.getObjectSpec().right()

            // Class annotated with @Dowel annotation
            propTypeDeclaration.isDowelClass() -> propTypeDeclaration.getDowelSpec().right()

            // Classes with no-args constructor
            propTypeDeclaration.hasNoArgsConstructor() ->
                propTypeDeclaration.getNoArgsConstructorSpec().right()

            // Compose types
            // State
            stateDeclaration.isAssignableFrom(propType) -> propType.getStateSpec()

            // Color
            colorDeclaration.isAssignableFrom(propType) -> ColorSpec.right()

            // Unsupported types which are nullable
            propType.isMarkedNullable -> getUnsupportedNullableSpec().right()

            else -> UnsupportedType.left()
        }

        return paramSpec
    }

    private fun KSValueParameter.mapToParameter(
        spec: ParameterSpec,
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

        val provider: KSClassDeclaration? = predefinedProviders[this.makeNotNullable()]
        requireNotNull(provider) { "Dowel internal error, something went wrong while processing predefined param providers for $this" }

        return PreDefinedProviderSpec(
            provider = provider.toClassName(),
            type = this,
            propertyName = this.getBackingListPropertyName(),
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
            range = range,
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

    private fun KSType.getListSpec(
        annotations: List<KSAnnotation>,
    ): MaybeSpec<ListSpec> {

        val size: Size = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_LIST_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_LIST_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_LIST_LEN_MAX,
        )

        require(this.arguments.size == 1) { "List must have have exactly one type argument. Current size = ${this.arguments.size}" }

        val arg = this.arguments.first()
        val resolvedType = arg.type!!.resolve()
        val spec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return either {
            ListSpec(
                size = size,
                elementSpec = spec.bind(),
            )
        }
    }

    private fun KSType.getSetSpec(
        annotations: List<KSAnnotation>,
    ): MaybeSpec<SetSpec> {

        val size: Size = Size.find(
            annotations = annotations.toList(),
            defaultValue = DefaultRange.DEFAULT_LIST_LEN_VALUE,
            defaultMin = DefaultRange.DEFAULT_LIST_LEN_MIN,
            defaultMax = DefaultRange.DEFAULT_LIST_LEN_MAX,
        )

        require(this.arguments.size == 1) { "Set must have have exactly one type argument. Current size = ${this.arguments.size}" }

        val arg = this.arguments.first()
        val resolvedType = arg.type!!.resolve()
        val spec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return either {
            SetSpec(
                size = size,
                elementSpec = spec.bind(),
            )
        }
    }

    private fun KSType.getMapSpec(
        annotations: List<KSAnnotation>,
    ): MaybeSpec<MapSpec> {

        fun KSTypeArgument.getSpec(): MaybeParamSpec {
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

        return either {
            MapSpec(
                size = size,
                keySpec = key.bind(),
                valueSpec = value.bind(),
            )
        }
    }

    private fun KSType.getFlowSpec(): MaybeSpec<FlowSpec> {

        require(this.arguments.size == 1) { "Flow must have have exactly one type argument. Current size = ${this.arguments.size}" }

        val arg = this.arguments.first()

        val resolvedType = arg.type!!.resolve()
        val spec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return either {
            FlowSpec(
                elementSpec = spec.bind(),
            )
        }
    }

    private fun KSType.getPairSpec(): MaybeSpec<PairSpec> {

        fun KSTypeArgument.getSpec(): MaybeParamSpec {
            val resolvedType = this.type!!.resolve()
            return resolvedType.getSpec(
                annotations = this.annotations.toList(),
            )
        }

        require(this.arguments.size == 2) { "Pair must have have exactly two type arguments. Current size = ${this.arguments.size}" }

        val left = this.arguments[0].getSpec()
        val right = this.arguments[1].getSpec()

        return either {
            PairSpec(
                leftElementSpec = left.bind(),
                rightElementSpec = right.bind(),
            )
        }
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

    private fun KSClassDeclaration.getSealedSpec(): MaybeSpec<SealedSpec> {

        fun MaybeSpec<ParameterSpec>.logErrorIfInvalid(
            declaration: KSClassDeclaration,
        ) {
            when (this) {
                is Either.Left -> {
                    logger.error(
                        message = "\nSealed sub types can only be Objects, Enum classes, or concrete classes annotated with @${Dowel::class.simpleName} annotation",
                        symbol = declaration,
                    )
                }
                is Either.Right -> Unit
            }
        }

        // This is an expensive call
        val subClasses: List<KSClassDeclaration> = this.getSealedSubclasses().toList()

        if (subClasses.isEmpty()) {
            logger.error(
                message = "\nSealed type ${this.qualifiedName!!.asString()} does not have any concrete implementations.\n" +
                    "There should be al-least a single implementation of a sealed type present in order to be able to provide an instance.",
                symbol = this,
            )
            return UnsupportedType.left()
        }

        val subTypeSpecs: List<MaybeSpec<ParameterSpec>> = subClasses.map { declaration ->

            // Using star projected type for now, as classes with generic type parameters
            // are not supported yet.
            // TODO: Would have to reconsider this choice, when adding support for classes with generic type parameters.
            val type: KSType = declaration.asStarProjectedType()

            // The type.getSpec() method will trigger calling of this method again if any
            // of the sub-classes is a sealed class again
            val spec = type.getSpec(annotations = declaration.annotations.toList())
            spec.logErrorIfInvalid(declaration = declaration)
            spec
        }

        val sealedSpec: Either<UnsupportedType, SealedSpec> = either {
            val subTypes: List<ParameterSpec> = subTypeSpecs.map { subClass -> subClass.bind() }
            SealedSpec(
                subTypeSpecs = subTypes,
            )
        }

        return sealedSpec
    }

    private fun KSClassDeclaration.getEnumSpec(): EnumSpec {

        return EnumSpec(enumDeclaration = this)
    }

    private fun KSClassDeclaration.getObjectSpec(): ObjectSpec {

        return ObjectSpec(objectDeclaration = this)
    }

    private fun KSDeclaration.getDowelSpec(): DowelSpec {

        val declaration = this as KSClassDeclaration
        val type = declaration.asType(listOf())

        val provider = ClassName(
            packageName = declaration.packageName.asString(),
            declaration.dowelClassName
        )

        return DowelSpec(
            type = declaration.asType(listOf()), // TODO: Revisit this
            provider = provider,
            propertyName = type.getBackingListPropertyName(),
        )
    }

    private fun KSDeclaration.getNoArgsConstructorSpec(): NoArgsConstructorSpec {
        val declaration = this as KSClassDeclaration
        return NoArgsConstructorSpec(
            classDeclarations = declaration,
        )
    }

    private fun KSType.getStateSpec(): MaybeSpec<StateSpec> {

        require(this.arguments.size == 1) { "State must have have exactly one type argument. Current size = ${this.arguments.size}" }
        val arg = this.arguments.first()

        val resolvedType = arg.type!!.resolve()
        val spec = resolvedType.getSpec(
            annotations = arg.annotations.toList(),
        )

        return either {
            StateSpec(
                elementSpec = spec.bind(),
            )
        }
    }

    private fun getUnsupportedNullableSpec(): UnsupportedNullableSpec = UnsupportedNullableSpec

    private fun KSDeclaration.isDowelClass(): Boolean {
        val declaration = this as? KSClassDeclaration ?: return false
        val dowelName = Dowel::class.java.simpleName
        val annotation = declaration.annotations.find { it.shortName.asString() == dowelName }
        return annotation != null
    }

    private fun KSDeclaration.hasNoArgsConstructor(): Boolean {
        val declaration = this as? KSClassDeclaration ?: return false
        val constructors = declaration.getConstructors()

        val hasNoArgsConstructor = constructors
            .any { constructor -> constructor.parameters.isEmpty() }
        if (hasNoArgsConstructor) return true

        val hasAllDefaultValueConstructor = constructors
            .any { constructor -> constructor.parameters.all { param -> param.hasDefault } }
        if (hasAllDefaultValueConstructor) return true

        return false
    }

    private fun KSPLogger.logUnsupportedTypeError(
        parentClass: KSClassDeclaration,
        parameter: KSValueParameter,
        type: KSType,
    ) {

        val logger = this

        val location = parentClass.simpleName.asString() + "." + parameter.name!!.asString()
        val message = "\nUnexpected type encountered : $type @ $location.\n" +
            "See documentation of @${Dowel::class.simpleName} annotation class to read more about " +
            "the supported types or how to potentially fix this issue.\n" +
            "Alternatively, provide a pre-defined PreviewParameterProvider via the " +
            "@${ConsiderForDowel::class.simpleName} annotation."

        logger.error(message = message, symbol = parameter)
    }

    private fun KSType.getBackingListPropertyName(): String {

        return this.toClassName().relativeClassName
            .filter { it.isLetterOrDigit() }
            .replaceFirstChar { char -> char.lowercaseChar() } + "List"
    }
}
