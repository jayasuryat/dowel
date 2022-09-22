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
class WrongDowelListUsageDetectorTest {

    private val issue: Issue = WrongDowelListUsageDetector.IssueInfo.Definition

    @Suppress("PrivatePropertyName")
    private val DowelStub = kotlin(
        """
        package com.jayasuryat.dowel.annotation
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS)
        annotation class Dowel(
            val count: Int = DEFAULT_COUNT,
        )

        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS)
        annotation class DowelList(
            val count: Int = DEFAULT_COUNT,
        )
        """.trimIndent()
    )

    @Test
    fun `check dowelList with a dowel class`() {

        TestLintTask.lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    import com.jayasuryat.dowel.annotation.DowelList
                    @Dowel
                    @DowelList
                    class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectClean()
    }

    @Test
    fun `check dowelList with a dowel class reordered`() {

        TestLintTask.lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    import com.jayasuryat.dowel.annotation.DowelList
                    @DowelList
                    @Dowel
                    class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectClean()
    }

    @Test
    fun `check dowelList with a non-dowel class`() {

        TestLintTask.lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    import com.jayasuryat.dowel.annotation.DowelList
                    @DowelList
                    class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:4: Error: ${WrongDowelListUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @DowelList
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
