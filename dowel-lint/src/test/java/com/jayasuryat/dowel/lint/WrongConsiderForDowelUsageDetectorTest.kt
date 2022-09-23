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
import org.junit.Test

@Suppress("UnstableApiUsage")
class WrongConsiderForDowelUsageDetectorTest {

    private val issue: Issue = WrongConsiderForDowelUsageDetector.IssueInfo.Definition

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

        TestLintTask.lint()
            .files(
                ConsiderForDowelStub,
                PreviewParameterProviderStub,
                kotlin(
                    """
                    package dowel

                    import androidx.compose.ui.tooling.preview.PreviewParameterProvider
                    import com.jayasuryat.dowel.annotation.ConsiderForDowel

                    @ConsiderForDowel
                    internal class CustomPreviewParamProvider : PreviewParameterProvider<String>
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should raise error for considerForDowel with no super class`() {

        TestLintTask.lint()
            .files(
                ConsiderForDowelStub,
                PreviewParameterProviderStub,
                kotlin(
                    """
                    package dowel

                    import androidx.compose.ui.tooling.preview.PreviewParameterProvider
                    import com.jayasuryat.dowel.annotation.ConsiderForDowel

                    @ConsiderForDowel
                    internal class CustomPreviewParamProvider
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/CustomPreviewParamProvider.kt:6: Error: ${WrongConsiderForDowelUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @ConsiderForDowel
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
