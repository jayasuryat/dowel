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
import org.jetbrains.uast.UElement

@Suppress("UnstableApiUsage")
internal class WrongDowelUsageDetector : Detector() {

    override fun applicableAnnotations(): List<String> = listOf(
        AnnotationNames.DOWEL
    )

    override fun visitAnnotationUsage(
        context: JavaContext,
        element: UElement,
        annotationInfo: AnnotationInfo,
        usageInfo: AnnotationUsageInfo,
    ) {

        context.report(
            ISSUE, element, context.getLocation(element),
            "This is a test lint issue. $element"
        )
    }

    companion object {

        /**
         * Issue describing the problem and pointing to the detector implementation.
         */
        private val ISSUE: Issue = Issue.create(
            // ID: used in @SuppressLint warnings etc
            id = "WrongDowelUsage",
            // Title -- shown in the IDE's preference dialog, as category headers in the
            // Analysis results window, etc
            briefDescription = "Improper usage of @Dowel annotation",
            // Full explanation of the issue; you can use some markdown markup such as
            // `monospace`, *italic*, and **bold**.
            explanation = """
                    Explain why / why-not to do a certain thing in a certain way.
                    """, // no need to .trimIndent(), lint does that automatically
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                WrongDowelUsageDetector::class.java,
                Scope.JAVA_FILE_SCOPE
            )
        )

        val issues: List<Issue> = listOf(ISSUE)
    }
}
