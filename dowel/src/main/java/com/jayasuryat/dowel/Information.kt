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
@file:Suppress("unused")

package com.jayasuryat.dowel

import com.jayasuryat.dowel.annotation.ConsiderForDowel
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList

/**
 * This is a just an empty container module to expose **:dowel-annotation** & **:dowel-lint**
 * modules together.
 *
 * This module was brought in as the gradle task `lintPublish` can only be applied to
 * android-libraries. But in case of Dowel - `:dowel-annotation` module is a Java/Kotlin library only.
 * So, instead of making the annotations module an Android library and exposing annotations and the
 * lint checks from there, this choice was made to keep the annotations library as Java/Kotlin module
 * only and expose those two modules from a container Android module. The benefit of keeping the
 * `:dowel-annotation` module as Java/Kotlin library only is that now other modules like
 * `:dowel-processor` and `:dowel-lint` (Java/Kotlin modules) can depend on `:dowel-annotation`
 * module and work with concrete types, instead of using strings for class and package names (of
 * annotations) for doing code generation and lint checks.
 *
 * * :dowel-annotation module : Contains all of the annotations
 * * :dowel-lint module : Contains custom lint checks for Dowel
 * * :dowel-processor module : Contains all of the processing logic of generating code
 *
 * @see [Dowel]
 * @see [DowelList]
 * @see [ConsiderForDowel]
 */
private object Information
