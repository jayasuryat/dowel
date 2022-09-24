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

        val evaluator = context.evaluator
        val parent: UClass = element.uastParent as? UClass ?: return

        val validSuperType: Boolean = context.evaluator.extendsClass(
            cls = parent.javaPsi,
            className = "androidx.compose.ui.tooling.preview.PreviewParameterProvider",
            strict = true
        )

        if (!validSuperType) {

            context.report(
                issue = MissingSuperTypeIssue.Definition,
                scope = element,
                location = context.getLocation(element),
                message = MissingSuperTypeIssue.MESSAGE,
            )
            return
        }

        // Class is private in file
        // The check is not evaluator.isPrivate(parent) because that's how Java representation works
        if (!evaluator.isPublic(parent)) {

            context.report(
                issue = PrivateClassIssue.Definition,
                scope = element,
                location = context.getLocation(element),
                message = PrivateClassIssue.MESSAGE,
            )
        }

        // Constructor is private
        val constructor = parent.constructors.firstOrNull()
        if (constructor == null || evaluator.isPrivate(constructor)) {

            context.report(
                issue = InaccessibleConstructorIssue.Definition,
                scope = element,
                location = context.getLocation(element),
                message = InaccessibleConstructorIssue.MESSAGE,
            )
        }
    }

    internal object MissingSuperTypeIssue {

        internal const val ISSUE_ID: String = "MissingSuperTypeForConsiderForDowel"

        internal const val MESSAGE: String =
            "@ConsiderForDowel annotation can only be applied to classes extending androidx.compose.ui.tooling.preview.PreviewParameterProvider"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@ConsiderForDowel annotation can only be applied to classes extending [androidx.compose.ui.tooling.preview.PreviewParameterProvider]. See documentation of @ConsiderForDowel for more information.",
            category = Category.CORRECTNESS,
            priority = 5,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongConsiderForDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    internal object PrivateClassIssue {

        internal const val ISSUE_ID: String = "PrivateDowelListClass"

        internal const val MESSAGE: String =
            "@ConsiderForDowel annotation can't be applied to a private classes"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@ConsiderForDowel annotation can't be applied to private classes as Dowel would be unable create instances for this class.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    internal object InaccessibleConstructorIssue {

        internal const val ISSUE_ID: String = "InaccessibleConstructorForConsiderForDowel"

        internal const val MESSAGE: String =
            "@ConsiderForDowel annotation can't be applied classes with private / inaccessible constructors"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@ConsiderForDowel annotation can't be applied to classes with private / inaccessible constructors as Dowel would be unable create instances for that class.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    companion object {

        internal val ISSUES: Array<Issue> = arrayOf(
            MissingSuperTypeIssue.Definition,
            PrivateClassIssue.Definition,
            InaccessibleConstructorIssue.Definition,
        )
    }
}
