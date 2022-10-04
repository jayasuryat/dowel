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
import com.intellij.lang.jvm.JvmClassKind
import com.intellij.psi.PsiModifier
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

        val evaluator = context.evaluator
        val parent: UClass = element.uastParent as? UClass ?: return

        // Class-kind is not 'class' or class is not concrete
        if (parent.classKind != JvmClassKind.CLASS ||
            evaluator.isAbstract(parent) ||
            evaluator.isCompanion(parent)
        ) {

            // TODO: This check does not check for objects, need to add that

            context.report(
                issue = InvalidClassKindIssue.Definition,
                scope = element,
                location = context.getLocation(element),
                message = InvalidClassKindIssue.MESSAGE,
            )

            return
        }

        // Inner class. (Nested and non-inner classes have the static modifier in the Java code)
        val declarationClass = parent.uastParent
        if (declarationClass != null &&
            declarationClass is UClass &&
            declarationClass.classKind == JvmClassKind.CLASS &&
            !parent.hasModifierProperty(PsiModifier.STATIC)
        ) {

            context.report(
                issue = InnerClassIssue.Definition,
                scope = element,
                location = context.getLocation(element),
                message = InnerClassIssue.MESSAGE,
            )
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

    internal object InvalidClassKindIssue {

        internal const val ISSUE_ID: String = "InvalidClassKindForDowel"

        internal const val MESSAGE: String =
            "@Dowel annotation can only be applied to concrete classes."

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@Dowel annotation should only be applied to concrete classes.",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    internal object InnerClassIssue {

        internal const val ISSUE_ID: String = "InnerClassForDowel"

        internal const val MESSAGE: String =
            "@Dowel annotation can't be applied to inner classes"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@Dowel annotation can't be applied to inner classes",
            category = Category.CORRECTNESS,
            priority = 7,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )
    }

    internal object PrivateClassIssue {

        internal const val ISSUE_ID: String = "PrivateDowelClass"

        internal const val MESSAGE: String =
            "@Dowel annotation can't be applied to private classes"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@Dowel annotation can't be applied to private classes as Dowel would be unable create instances for that class.",
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

        internal const val ISSUE_ID: String = "InaccessibleConstructorForDowel"

        internal const val MESSAGE: String =
            "@Dowel annotation can't be applied classes with private / inaccessible constructors"

        internal val Definition: Issue = Issue.create(
            id = ISSUE_ID,
            briefDescription = MESSAGE,
            explanation = "@Dowel annotation can't be applied to classes with private / inaccessible constructors as Dowel would be unable create instances for that class.",
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
            InvalidClassKindIssue.Definition,
            InnerClassIssue.Definition,
            PrivateClassIssue.Definition,
            InaccessibleConstructorIssue.Definition,
        )
    }
}
