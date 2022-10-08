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
package com.jayasuryat.dowel.processor.validator

import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration

/**
 * A container class to hold a set of validated symbols which are validated based on any particular
 * validation logic.
 * @param [valid] list of symbols which satisfy the said logic
 * @param [invalid] list of symbols which don't satisfy the said logic
 */
internal class ValidatedSymbols<T : KSAnnotated>(
    val valid: List<T>,
    val invalid: List<KSAnnotated>,
)

/**
 * A type alias to represent validated [KSClassDeclaration] which fit a certain validation logic.
 * @see [ValidatedSymbols]
 */
internal typealias ValidatedClassDeclarations = ValidatedSymbols<KSClassDeclaration>
