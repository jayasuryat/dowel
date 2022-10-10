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
package com.jayasuryat.dowel.lint

import com.android.tools.lint.checks.infrastructure.TestFiles.kotlin
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Issue
import com.jayasuryat.dowel.lint.WrongConsiderForDowelUsageDetector.*
import org.junit.Test

@Suppress("UnstableApiUsage")
class WrongConsiderForDowelUsageDetectorTest {

    private val issue: Array<Issue> = WrongConsiderForDowelUsageDetector.ISSUES

    @Suppress("PrivatePropertyName")
    private val ConsiderForDowelStub = kotlin(
        """
        package com.jayasuryat.dowel.annotation
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS)
        annotation class ConsiderForDowel
        """.trimIndent()
    )

    @Suppress("PrivatePropertyName")
    private val PreviewParameterProviderStub = kotlin(
        """
        package androidx.compose.ui.tooling.preview
        interface PreviewParameterProvider<T>
        """.trimIndent()
    )

    @Test
    fun `should be clean for considerForDowel with PreviewParameterProvider super class`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            internal class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should raise error for considerForDowel with no super class`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            internal class CustomPreviewParamProvider
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${MissingSuperTypeIssue.MESSAGE} [${MissingSuperTypeIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with abstract class`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            abstract class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with sealed class`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            sealed class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with interface`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            interface CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with inner class`() {

        val source = kotlin(
            """
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
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:11: Error: ${InnerClassIssue.MESSAGE} [${InnerClassIssue.ISSUE_ID}]
                    @ConsiderForDowel
                    ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with private class`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            private class CustomPreviewParamProvider : PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${PrivateClassIssue.MESSAGE} [${PrivateClassIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with class with private constructor`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            class CustomPreviewParamProvider private constructor(): PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${InaccessibleConstructorIssue.MESSAGE} [${InaccessibleConstructorIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with private class and private constructor`() {

        val source = kotlin(
            """
            package dowel

            import androidx.compose.ui.tooling.preview.PreviewParameterProvider
            import com.jayasuryat.dowel.annotation.ConsiderForDowel

            @ConsiderForDowel
            private class CustomPreviewParamProvider private constructor(): PreviewParameterProvider<String>
            """.trimIndent()
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${PrivateClassIssue.MESSAGE} [${PrivateClassIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for considerForDowel with non-empty constructor`() {

        val source = kotlin(
            """
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
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${UnInvokableConstructorIssue.MESSAGE} [${UnInvokableConstructorIssue.ISSUE_ID}]
                @ConsiderForDowel
                ~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should be clean for considerForDowel with non-empty constructor with default values`() {

        val source = kotlin(
            """
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
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should be clean for considerForDowel with secondary constructor`() {

        val source = kotlin(
            """
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
        )

        TestLintTask.lint()
            .files(ConsiderForDowelStub, PreviewParameterProviderStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }
}
