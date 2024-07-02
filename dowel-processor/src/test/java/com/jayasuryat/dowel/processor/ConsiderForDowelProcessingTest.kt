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
internal class ConsiderForDowelProcessingTest {

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
    fun `should compile success for considerForDowel with PreviewParameterProvider super class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            internal class CustomPreviewParamProvider : PreviewParameterProvider<String>{
                override val values : Sequence<String> = sequenceOf("" ,"" ,"" ) 
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for dowel and considerForDowel interop`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            import com.jayasuryat.dowel.annotation.Dowel
            
            @Dowel
            data class Person(
                val name : String,
                val location : Location,
            )
            
            data class Location(
                val lat : Long,
                val lon : Long,
            )
            
            @ConsiderForDowel
            internal class CustomPreviewParamProvider : PreviewParameterProvider<Location>{
                override val values : Sequence<Location> = sequenceOf() 
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with no super class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider 
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Class dowel.CustomPreviewParamProvider is annotated with @ConsiderForDowel, but does not extend androidx.compose.ui.tooling.preview.PreviewParameterProvider.

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with abstract class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            abstract class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Only concrete classes can be annotated with @ConsiderForDowel annotation

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with sealed class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            sealed class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()


        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Only concrete classes can be annotated with @ConsiderForDowel annotation

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with interface`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            interface CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()


        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Only concrete classes can be annotated with @ConsiderForDowel annotation

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with private class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            private class CustomPreviewParamProvider : PreviewParameterProvider<String>{
                override val values : Sequence<String> = sequenceOf("", "", "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Cannot create an instance for `CustomPreviewParamProvider` class: it is private in file.

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with private constructor`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider private constructor(): PreviewParameterProvider<String>{
                override val values : Sequence<String> = sequenceOf("", "", "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Classes annotated with @ConsiderForDowel must have at-least a single non-private constructor

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with non-empty constructor`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider(
                val param : String,
            ): PreviewParameterProvider<String>{
                override val values : Sequence<String> = sequenceOf("", "", "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:7: 
            Classes annotated with @ConsiderForDowel must have at-least a single no-args constructor and it must be non-private.
            If constructor parameters are necessary for this class, consider adding a secondary no-args constructor. Or specify default values to all of the properties of any of the constructor.

        """.trimIndent(), result.messages)
    }

    @Test
    fun `should compile success for considerForDowel with secondary constructor`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider(
                val param : String,
            ): PreviewParameterProvider<String>{
                
                constructor() : this("")
            
                override val values : Sequence<String> = sequenceOf("", "", "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for considerForDowel with secondary constructor with default values`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider(
                val param : String,
            ): PreviewParameterProvider<String>{
                
                constructor(param: Int = -1) : this(param.toString())
            
                override val values : Sequence<String> = sequenceOf("", "", "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should compile success for considerForDowel with non-empty constructor with default values`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            @ConsiderForDowel
            class CustomPreviewParamProvider(
                param1 : String = "",
                param2 : Int = -1,
            ): PreviewParameterProvider<String>{
                override val values : Sequence<String> = sequenceOf(param1, param2.toString(), "")
            }
            """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.OK, result.exitCode)
        Assert.assertEquals("", result.messages)
    }

    @Test
    fun `should raise error for considerForDowel with inner class`() {

        val source = """
            package dowel
            
            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel
            
            class Person(
                val name: String,
                val age: String,
            ){
                
                @ConsiderForDowel
                inner class CustomPreviewParamProvider : PreviewParameterProvider<String>{
                    override val values : Sequence<String> = sequenceOf("", "", "")
                }
            }
        """.trimIndent()

        val kotlinSource: SourceFile =
            SourceFile.kotlin(name = "CustomPreviewParamProvider.kt", contents = source)
        val result: KotlinCompilation.Result = compile(kotlinSource, PreviewParameterProviderStub)

        Assert.assertEquals(KotlinCompilation.ExitCode.COMPILATION_ERROR, result.exitCode)
        Assert.assertEquals("""
            e: Error occurred in KSP, check log for detail
            e: [ksp] ${temporaryFolder.root.path}/sources/CustomPreviewParamProvider.kt:12: 
            @ConsiderForDowel annotation can't be applied to inner classes

        """.trimIndent(), result.messages)
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
