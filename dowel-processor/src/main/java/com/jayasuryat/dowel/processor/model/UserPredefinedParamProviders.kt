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
package com.jayasuryat.dowel.processor.model

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.jayasuryat.dowel.annotation.ConsiderForDowel

/**
 * A type alias to represent all of the [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
 * annotated with @[ConsiderForDowel] annotation, mapped to their generic type parameter.
 *
 * For example,
 * ```
 *     @ConsiderForDowel
 *     class PersonProvider : PreviewParameterProvider<Person>() {...}
 * ```
 *
 * The above class would be mapped to -> Person to PersonProvider
 */
@Suppress("KDocUnresolvedReference")
internal typealias UserPredefinedParamProviders = Map<KSType, KSClassDeclaration>
