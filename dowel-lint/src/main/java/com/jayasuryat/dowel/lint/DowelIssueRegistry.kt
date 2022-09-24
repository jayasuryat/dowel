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

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.client.api.Vendor
import com.android.tools.lint.detector.api.CURRENT_API
import com.android.tools.lint.detector.api.Issue

@Suppress("UnstableApiUsage")
public class DowelIssueRegistry : IssueRegistry() {

    override val issues: List<Issue> = AllIssues

    override val api: Int
        get() = CURRENT_API

    override val vendor: Vendor = Vendor(
        vendorName = "JayaSuryaT/Dowel",
        feedbackUrl = "https://github.com/JayaSuryaT/Dowel/issues",
        identifier = "com.jayasuryat.dowel:dowel:{version}"
    )

    internal companion object {

        val AllIssues: List<Issue> = listOf(
            *WrongDowelUsageDetector.ISSUES,
            WrongDowelListUsageDetector.IssueInfo.Definition,
            WrongConsiderForDowelUsageDetector.IssueInfo.Definition,
        )
    }
}
