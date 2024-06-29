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
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCompilerApi::class)
@Suppress("PrivatePropertyName")
internal class DowelListProcessingTest {

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

    private val PreviewParameterProviderStub: SourceFile by lazy {
        val source = """
            package androidx.compose.ui.tooling.preview
            interface PreviewParameterProvider<T> {
                val values: Sequence<T>
            }
        """.trimIndent()
        SourceFile.kotlin(name = "PreviewParameterProvider.kt", contents = source)
    }

    @Test
    fun `should compile success for dowelList with dowel class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            import com.jayasuryat.dowel.annotation.DowelList
            
            @DowelList
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
    fun `should compile success for dowelList with dowel class reordered`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            import com.jayasuryat.dowel.annotation.DowelList
            
            @Dowel
            @DowelList
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
    fun `should raise error for dowelList with non-dowel class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.DowelList
            
            @DowelList
            class Person(
                val name: String,
                val age: String,
            )
        """.trimIndent()

        val kotlinSource: SourceFile = SourceFile.kotlin(name = "Person.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/Person.kt:6: 
            @DowelList annotation can only be applied to classes already annotated with @Dowel annotation.

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should compile success for dowelList with internal class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            import com.jayasuryat.dowel.annotation.DowelList
            
            @DowelList
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
    fun `should compile success for dowelList with nested internal class`() {

        val source = """
            package dowel
            
            import com.jayasuryat.dowel.annotation.Dowel
            import com.jayasuryat.dowel.annotation.DowelList
            
            @DowelList
            @Dowel
            internal data class Person(
                val name: String,
                val age: String,
            ){
            
                @DowelList
                @Dowel
                data class Location(
                    val lat : Long,
                    val lon : Long,
                ){

                    @DowelList
                    @Dowel
                    data class GeoData(
                        val data : Long,
                    )
                }
            }
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
