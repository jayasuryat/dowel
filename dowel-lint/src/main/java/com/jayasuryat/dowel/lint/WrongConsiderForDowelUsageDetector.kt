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

import com.android.tools.lint.detector.api.*
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

@Suppress("UnstableApiUsage")
internal class WrongConsiderForDowelUsageDetector : Detector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> {
        return listOf(AnnotationNames.ConsiderForDowel)
    }

    override fun isApplicableAnnotationUsage(type: AnnotationUsageType): Boolean {
        return type == AnnotationUsageType.DEFINITION
    }

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {

        val parent: UClass = element.uastParent as? UClass ?: return

        val validSuperType: Boolean = context.evaluator.extendsClass(
            cls = parent.javaPsi,
            className = "androidx.compose.ui.tooling.preview.PreviewParameterProvider",
            strict = true
        )

        if (!validSuperType) {

            context.report(
                issue = IssueInfo.Definition,
                scope = element,
                location = context.getLocation(parent as UElement),
                message = IssueInfo.MESSAGE,
            )
        }
    }

    internal object IssueInfo {

        @Suppress("MemberVisibilityCanBePrivate")
        internal const val ISSUE_ID: String = "WrongConsiderForDowelUsage"

        internal const val MESSAGE: String =
            "@ConsiderForDowel annotation can only be applied to classes extending androidx.compose.ui.tooling.preview.PreviewParameterProvider"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = "Invalid usage of @ConsiderForDowel annotation.",
            explanation = """
                    @ConsiderForDowel annotation can only be applied to classes extending
                    [androidx.compose.ui.tooling.preview.PreviewParameterProvider]. See documentation
                    of @ConsiderForDowel for more information.
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongConsiderForDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
