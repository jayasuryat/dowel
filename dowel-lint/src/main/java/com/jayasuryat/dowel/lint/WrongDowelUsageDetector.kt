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
internal class WrongDowelUsageDetector : Detector(), SourceCodeScanner {

    override fun applicableAnnotations(): List<String> {
        return listOf(AnnotationNames.Dowel)
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

        val isInvalidUsage = context.evaluator.isAbstract(parent) ||
            context.evaluator.isCompanion(parent)

        if (isInvalidUsage) {

            context.report(
                issue = IssueInfo.Definition,
                scope = element,
                location = context.getLocation(parent as UElement),
                message = IssueInfo.MESSAGE,
            )
        }
    }

    internal object IssueInfo {

        internal const val MESSAGE: String =
            "@Dowel annotation can only be applied to concrete classes."

        internal const val ISSUE_ID: String = "WrongDowelUsage"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = "Invalid usage of @Dowel annotation.",
            explanation = """
                    @Dowel annotation should only be applied to concrete classes.
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }
}
