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

import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
import com.tschuchort.compiletesting.kspWithCompilation
import com.tschuchort.compiletesting.symbolProcessorProviders
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@Suppress("PrivatePropertyName", "RedundantSuppression")
internal class DowelWholeEnchiladaProcessingTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val processorProvider: SymbolProcessorProvider by lazy {
        DowelSymbolProcessorProvider()
    }

    // spotless:off

    //region : Sources
    private val PreviewParameterProviderStub: SourceFile by lazy {
        val source = """
            package androidx.compose.ui.tooling.preview
            interface PreviewParameterProvider<T> {
                val values: Sequence<T>
            }
        """.trimIndent()
        SourceFile.kotlin(name = "PreviewParameterProvider.kt", contents = source)
    }

    private val SizeStub: SourceFile by lazy {
        val source = """
            package androidx.annotation
            
            annotation class Size(
                val value : Long = -1,
                val min : Long = Long.MIN_VALUE,
                val max : Long = Long.MAX_VALUE,
                val multiple : Long = 1,
            )
        """.trimIndent()
        SourceFile.kotlin(name = "Size.kt", contents = source)
    }

    private val FlowStub: SourceFile by lazy {
        val source = """
            package kotlinx.coroutines.flow
            interface Flow<T>
            interface SharedFlow<T> : Flow<T>
            interface MutableSharedFlow<T> : SharedFlow<T>
            interface StateFlow<T> : SharedFlow<T>
            interface MutableStateFlow<T> : StateFlow<T>, MutableSharedFlow<T>
            
            @Suppress("TestFunctionName", "RedundantSuppression", "UNUSED_PARAMETER")
            fun <T> MutableStateFlow(vararg elements: T): MutableStateFlow<T> = object : MutableStateFlow<T> {}
        """.trimIndent()
        SourceFile.kotlin(name = "Flow.kt", contents = source)
    }

    private val ImmutableCollectionsStub: SourceFile by lazy {
        val source = """
            @file:Suppress("UNUSED_PARAMETER")

            package kotlinx.collections.immutable
            interface ImmutableList<out E>
            interface PersistentList<out E> : ImmutableList<E>
            interface ImmutableSet<out E>
            interface PersistentSet<out E> : ImmutableSet<E>
            interface ImmutableMap<K, out V>
            interface PersistentMap<K, out V> : ImmutableMap<K, V>
            
            @Suppress("TestFunctionName", "RedundantSuppression")
            fun <E> persistentListOf(vararg elements: E): PersistentList<E> = object : PersistentList<E> {}
            fun <E> persistentSetOf(vararg elements: E): PersistentSet<E> = object : PersistentSet<E> {}
            fun <K, V> persistentMapOf(vararg pairs: Pair<K, V>): PersistentMap<K, V> = object : PersistentMap<K, V> {}
        """.trimIndent()
        SourceFile.kotlin(name = "ImmutableCollections.kt", contents = source)
    }

    private val StateStub: SourceFile by lazy {
        val source = """
            package androidx.compose.runtime
            interface State<T>
            interface MutableState<T> : State<T>
            
            @Suppress("UNUSED_PARAMETER")
            fun <T> mutableStateOf(value: T): MutableState<T> = object : MutableState<T> {}
        """.trimIndent()
        SourceFile.kotlin(name = "SnapshotState.kt", contents = source)
    }

    private val ColorStub: SourceFile by lazy {
        val source = """
            package androidx.compose.ui.graphics
            
            class Color(val value: ULong)
            
            @Suppress("TestFunctionName", "RedundantSuppression")
            fun Color(color: Long): Color {
               return Color(value = (color.toULong() and 0xffffffffUL) shl 32)
            }
        """.trimIndent()
        SourceFile.kotlin(name = "Color.kt", contents = source)
    }

    private val VehicleSource: SourceFile by lazy {
        val source = """
            package dowel.vehicle
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            sealed interface Vehicle {
            
                object Dummy : Vehicle
            
                @Dowel
                data class Bike(
                    val make: String,
                    val model: Int,
                ) : Vehicle
            
                @Dowel
                data class Car(
                    val make: String,
                    val model: Int,
                ) : Vehicle
            
                sealed interface FourWheeler : Vehicle {
            
                    @Dowel
                    data class Car(
                        val make: String,
                        val model: Int,
                    ) : FourWheeler
            
                    @Dowel
                    data class Truck(
                        val make: String,
                        val model: Int,
                        val bedCapacity: Float,
                    ) : FourWheeler
                }
            }
        """.trimIndent()
        SourceFile.kotlin(name = "Vehicle.kt", contents = source)
    }

    private val StatusSource: SourceFile by lazy {
        val source = """
            package dowel.status
            enum class Status {
                Active, InActive, PendingActivation, Deleted, Archived
            }
        """.trimIndent()
        SourceFile.kotlin(name = "Status.kt", contents = source)
    }

    private val StaticInfoSource: SourceFile by lazy {
        val source = """
            package dowel.static.info
            object SomeStaticInfo {
                const val someInfo: Int = 2
            }
        """.trimIndent()
        SourceFile.kotlin(name = "SomeStaticInfo.kt", contents = source)
    }

    private val LocationSource: SourceFile by lazy {
        val source = """
            package dowel.location
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            internal data class Location(
                val lat: Long?,
                val lon: Long?,
            )
        """.trimIndent()
        SourceFile.kotlin(name = "Location.kt", contents = source)
    }

    // TODO: Add support for class name clashes
    /*private val AmbiguityLocationSource: SourceFile by lazy {
        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            data class Location(
                val lat: Long,
                val lon: Long,
            )
        """.trimIndent()
        SourceFile.kotlin(name = "AmbiguousLocation.kt", contents = source)
    }*/
    //endregion

    @Test
    fun `throw everything at dowel`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            import androidx.annotation.Size
            import androidx.compose.runtime.State
            import androidx.compose.runtime.MutableState
            import kotlinx.coroutines.flow.*
            import kotlinx.collections.immutable.*
            import dowel.status.Status
            import dowel.vehicle.Vehicle
            import dowel.static.info.SomeStaticInfo
            import dowel.location.Location
            import androidx.compose.ui.graphics.Color
            
            @Dowel(count = 30)
            internal data class Person(
                @Size(value = 5) val name: String,
                @Size(value = 300) val bio: String?,
                val age: Int,
                val status: Status,
                val weight: Float,
                val height: Double,
                val likes: Long,
                val isAlien: Boolean,
                val location: Location,
                val vehicle: Vehicle,
                val liveLocation: Flow<Location?>,
                val latLon: Pair<Long, Long>,
                val info: SomeStaticInfo,
                val bannerColor : Color,
                @Size(value = 2) val locations: List<Location>,
                @Size(value = 2) val mutableLocations: MutableList<Location>,
                @Size(value = 3) val uniqueLocations: Set<Location>,
                @Size(value = 3) val uniqueMutableLocations: MutableSet<Location>,
                val isExpanded: State<Boolean>,
                val mutableIsExpanded: MutableState<Boolean>,
                @Size(value = 1) val preferences: Map<Long, Location>,
                @Size(value = 1) val mutablePreferences: MutableMap<Long, Location>,
                @Size(value = 2) val preferredLocations: Map<Long, Set<Location>>,
                @Size(value = 2) val preferredMutableLocations: Map<Long, MutableSet<Location>>,
                val title: Char,
                @Size(value = 1) val interests: List<Float>,
                @Size(value = 2) val uniqueInterests: Set<Float>,
                val meta : MetaInfo,
                val subjects : Subjects,
                val flow1: Flow<Int>,
                val flow2: SharedFlow<Int>,
                val flow3: MutableSharedFlow<Int>,
                val flow4: StateFlow<Int>,
                val flow5: MutableStateFlow<Int>,
                val immutableList: ImmutableList<Int>,
                val persistentList: PersistentList<Int>,
                val immutableSet: ImmutableSet<Int>,
                val persistentSet: PersistentSet<Int>,
                val immutableMap: ImmutableMap<Int, Int>,
                val persistentMap: PersistentMap<Int, Int>,
                val onClick: suspend (a: Person, b: Int) -> Unit,
            ){
                
                class MetaInfo {
                    var info : Long = -1
                }
            
                data class Subjects(
                    val subjects : List<String> = emptyList(),
                )
            }
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
            SizeStub,
            FlowStub,
            ImmutableCollectionsStub,
            StateStub,
            VehicleSource,
            StatusSource,
            StaticInfoSource,
            LocationSource,
            ColorStub,
            /*AmbiguityLocationSource*/
        )

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    // spotless:on

    private fun compile(
        vararg sourceFiles: SourceFile,
    ): KotlinCompilation.Result {

        fun prepareCompilation(
            vararg sourceFiles: SourceFile,
        ): KotlinCompilation {
            return KotlinCompilation().apply {
                workingDir = temporaryFolder.root
                inheritClassPath = true
                symbolProcessorProviders = listOf(processorProvider)
                sources = sourceFiles.asList()
                verbose = false
                kspWithCompilation = true
            }
        }

        return prepareCompilation(*sourceFiles)
            .compile()
    }
}
