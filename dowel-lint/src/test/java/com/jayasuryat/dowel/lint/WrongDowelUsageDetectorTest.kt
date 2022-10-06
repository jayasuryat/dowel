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
import com.jayasuryat.dowel.lint.WrongDowelUsageDetector.*
import org.junit.Test

@Suppress("UnstableApiUsage")
class WrongDowelUsageDetectorTest {

    private val issue: Array<Issue> = WrongDowelUsageDetector.ISSUES

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
    fun `should be clean for dowel with class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            class Person(
                val age : Int,
            )
            """.trimIndent()
        )

        lint().files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should be clean for dowel with data class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            data class Person(
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should be clean for dowel with internal class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            internal class Person(
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectClean()
    }

    @Test
    fun `should raise error for dowel with abstract class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            abstract class Person(
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with sealed class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            sealed class Person(
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with interface`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            interface Person
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with annotation class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            annotation class Person
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InvalidClassKindIssue.MESSAGE} [${InvalidClassKindIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with inner class`() {

        val source = kotlin(
            """
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
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:9: Error: ${InnerClassIssue.MESSAGE} [${InnerClassIssue.ISSUE_ID}]
                    @Dowel
                    ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with private class`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            private data class Person(
                val name : String,
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${PrivateClassIssue.MESSAGE} [${PrivateClassIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise error for dowel with class with private constructor`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            class Person private constructor(
                val name : String,
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InaccessibleConstructorIssue.MESSAGE} [${InaccessibleConstructorIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    @Test
    fun `should raise errors for dowel with private class with private constructor`() {

        val source = kotlin(
            """
            package dowel
            import com.jayasuryat.dowel.annotation.Dowel
            @Dowel
            private class Person private constructor(
                val name : String,
                val age : Int,
            )
            """.trimIndent()
        )

        lint()
            .files(DowelStub, source)
            .issues(*issue)
            .run()
            .expectContains(
                """
                src/dowel/Person.kt:3: Error: ${InaccessibleConstructorIssue.MESSAGE} [${InaccessibleConstructorIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                src/dowel/Person.kt:3: Error: ${PrivateClassIssue.MESSAGE} [${PrivateClassIssue.ISSUE_ID}]
                @Dowel
                ~~~~~~
                2 errors, 0 warnings
                """.trimIndent()
            )
    }

    /*
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
