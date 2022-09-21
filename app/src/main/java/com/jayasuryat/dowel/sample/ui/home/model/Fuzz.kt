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
package com.jayasuryat.dowel.sample.ui.home.model

import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList

@Dowel
private object Test1

@Dowel
private data class Test2(
    val x: Int = 2,
)

@DowelList
private data class Test28(
    val x: Int = 2,
)

@Dowel
private abstract class Test3 {
    val x: String = ""
}

@Dowel
private sealed class Test5 {
    val x: String = ""
}

@Dowel
private interface Test4

@Dowel
private sealed interface Test6

@Dowel
annotation class Test8
