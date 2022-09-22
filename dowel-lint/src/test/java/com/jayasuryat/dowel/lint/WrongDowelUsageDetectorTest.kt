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
import com.android.tools.lint.checks.infrastructure.TestLintTask.lint
import com.android.tools.lint.detector.api.Issue
import org.junit.Test

@Suppress("UnstableApiUsage")
class WrongDowelUsageDetectorTest {

    private val issue: Issue = WrongDowelUsageDetector.IssueInfo.Definition

    @Suppress("PrivatePropertyName")
    private val DowelStub = kotlin(
        """
        package com.jayasuryat.dowel.annotation
        @Retention(AnnotationRetention.SOURCE)
        @Target(AnnotationTarget.CLASS)
        annotation class Dowel(
            val count: Int = DEFAULT_COUNT,
        )
        """.trimIndent()
    )

    @Test
    fun `check dowel with class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
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
    fun `check dowel with data class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    data class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectClean()
    }

    @Test
    fun `check dowel with internal class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    internal class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectClean()
    }

    @Test
    fun `check dowel with abstract class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    abstract class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${WrongDowelUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @Dowel
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `check dowel with sealed class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    sealed class Person(
                        val age : Int,
                    )
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${WrongDowelUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @Dowel
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `check dowel with interface`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    interface Person
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${WrongDowelUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @Dowel
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `check dowel with annotation class`() {

        lint()
            .files(
                DowelStub,
                kotlin(
                    """
                    package dowel
                    import com.jayasuryat.dowel.annotation.Dowel
                    @Dowel
                    annotation class Person
                    """.trimIndent()
                )
            ).issues(issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${WrongDowelUsageDetector.IssueInfo.MESSAGE} [${issue.id}]
                @Dowel
                ^
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    /*

    TODO: Need to add support for checking for private class

   @Test
   fun `check dowel with private class`() {

       lint()
           .files(
               DowelStub,
               kotlin(
                   """
                   package dowel
                   import com.jayasuryat.dowel.annotation.Dowel
                   @Dowel
                   private class Person(
                       val age : Int,
                   )
                   """.trimIndent()
               )
           ).issues(issue)
           .run()
           .expectClean()
   }

   TODO: Need to add support for checking for objects

   @Test
   fun `check dowel with object`() {

       lint()
           .files(
               DowelStub,
               kotlin(
                   """
                   package dowel
                   import com.jayasuryat.dowel.annotation.Dowel
                   @Dowel
                   object Person(
                       val age : Int,
                   )
                   """.trimIndent()
               )
           ).issues(issue)
           .run()
           .expectClean()
   }
    */
}
