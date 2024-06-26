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

@Suppress("PrivatePropertyName")
internal class DowelProcessingTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val processorProvider: SymbolProcessorProvider by lazy {
        DowelSymbolProcessorProvider()
    }

    // The following comment turns off spotless checks until the 'on' command is detected.
    // Turning off spotless as it is formatting and removing trailing whitespaces in 'expected'
    // output strings and leading to failed tests.
    // spotless:off

    // region : Stubs
    private val PreviewParameterProviderStub: SourceFile by lazy {
        val source = """
            package androidx.compose.ui.tooling.preview
            interface PreviewParameterProvider<T> {
                val values: Sequence<T>
            }
        """.trimIndent()
        SourceFile.kotlin(name = "PreviewParameterProvider.kt", contents = source)
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
    // endregion

    @Test
    fun `should compile success for dowel with class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for dowel with data class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            data class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for dowel with internal class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            internal class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for dowel with nested internal class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            internal data class Person(
                val name: String,
                val age: String,
                val location : Location,
                val geoData : Location.GeoData,
            ){
            
                @Dowel
                data class Location(
                    val lat : Long,
                    val lon : Long,
                ){
                
                    @Dowel
                    data class GeoData(
                        val data : Long,
                    )
                }
            }
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
            ImmutableCollectionsStub,
        )

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for dowel with non-dowel types with no-args constructor`() {


        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            internal data class Person(
                val name: String,
                val age: String,
                val location : Location,
                val geoData : Location.GeoData,
            ){
            
                class Location {
                    val lat : Long = 0
                    val lon : Long = 0
                
                    data class GeoData(
                        val data : Long = -1,
                    )                
                }
            }
        """.trimIndent()
        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
            ImmutableCollectionsStub,
        )

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should raise error for dowel with abstract class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            abstract class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can't be applied to an abstract classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with sealed class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            sealed class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can't be applied to an abstract classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with interface`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            interface Person
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can only be applied to classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with object`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            object Person
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can only be applied to classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with annotation class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            annotation class Person
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can only be applied to classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with inner class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person(
                val name: String,
                val age: String,
            ){
                
                @Dowel
                inner class Author(
                    val id : Long,
                )
            }
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:12: 
            @Dowel annotation can't be applied to inner classes

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with private class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            private class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel cannot create an instance for `Person` class: it is private in file.

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with private constructor`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person private constructor(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            Cannot create an instance of class Person as it's constructor is private.
            @Dowel generates code based on the primary constructor of the annotated class, read more at Dowel annotation class's documentation.

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with class with generic type parameters`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person<T>(
                val name: String,
                val age: String,
                val data : T,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(
            """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @Dowel annotation can't be applied classes with generic type parameters.

            """.trimIndent(),
            result.messages
        )
    }

    @Test
    fun `should raise error for dowel with class with no-op sealed property`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person(
                val name: String,
                val age: String,
                val data : Data,
            )
            
            sealed class Data
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
            ImmutableCollectionsStub,
        )

        val expectedMessage = """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:12: 
            Sealed type dowel.Data does not have any concrete implementations.
            There should be al-least a single implementation of a sealed type present in order to be able to provide an instance.
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:9: 
            Unexpected type encountered : Data @ Person.data.
            See documentation of @Dowel annotation class to read more about the supported types or how to potentially fix this issue.
            Alternatively, provide a pre-defined PreviewParameterProvider via the @ConsiderForDowel annotation.

        """.trimIndent()

        Assert.assertTrue(result.messages.contains(expectedMessage))
    }

    @Test
    fun `should raise error for dowel with class with a single empty interface sub class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person(
                val name: String,
                val age: String,
                val data : Data,
            )
            
            sealed class Data{
                interface SubData : Data
            }
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
            ImmutableCollectionsStub,
        )

        val expectedMessage = """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:13: 
            Sealed sub types can only be Objects, Enum classes, or concrete classes annotated with @Dowel annotation
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:9: 
            Unexpected type encountered : Data @ Person.data.
            See documentation of @Dowel annotation class to read more about the supported types or how to potentially fix this issue.
            Alternatively, provide a pre-defined PreviewParameterProvider via the @ConsiderForDowel annotation.

        """.trimIndent()

        Assert.assertTrue(result.messages.contains(expectedMessage))
    }

    @Test
    fun `should raise error for dowel with Nothing property type`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            class Person(
                val name: String,
                val data : Nothing,
                val height : Double,
            )
            
            class Test
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(
            kotlinSource,
            PreviewParameterProviderStub,
        )

        val expectedMessage = """
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:8: 
            Unexpected type encountered : Nothing @ Person.data.
            See documentation of @Dowel annotation class to read more about the supported types or how to potentially fix this issue.
            Alternatively, provide a pre-defined PreviewParameterProvider via the @ConsiderForDowel annotation.

        """.trimIndent()

        Assert.assertTrue(result.messages.contains(expectedMessage))
    }

    @Test
    fun `should compile success for dowel with overrideDefaultValues`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel(overrideDefaultValues = true)
            internal class Person(
                val name: String = "",
                val age: String,
            )
            
            @Dowel(overrideDefaultValues = false)
            internal class Address(
                val name: String = "",
                val place: String,
            )
            
            @Dowel
            internal class SomeData(
                val name: String = "",
                val data: String,
            )

        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }
    //spotless:on

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
