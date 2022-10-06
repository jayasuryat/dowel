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
package com.jayasuryat.dowel.processor.util

import com.google.devtools.ksp.getVisibility
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Visibility

/**
 * Returns effective module visibility of the receiver [KSClassDeclaration]. A class could be nested
 * inside another class (not inner class, just nested), is such cases parent [KSClassDeclaration]'s
 * visibility is also considered (recursively).
 *
 * Throws error if the receiver class has unexpected visibility.
 */
internal fun KSClassDeclaration.getEffectiveModuleVisibility(): Visibility {

    val parent = this.parentDeclaration as? KSClassDeclaration

    // Getting parent class's visibility
    val parentVisibility: Visibility =
        parent?.getEffectiveModuleVisibility() ?: Visibility.PUBLIC

    // If the parent class's visibility is internal, then all of the nested classes effectively
    // become internal, and their visibility is need not to be evaluated. The only other case is
    // when the nested class is a private class, but Dowel doesn't really deal with private classes
    // so it would not be a valid case.
    if (parentVisibility == Visibility.INTERNAL) return Visibility.INTERNAL

    val localVisibility: Visibility = when (val visibility = this.getVisibility()) {
        Visibility.PUBLIC,
        Visibility.INTERNAL,
        -> visibility
        else -> error("Dowel internal error. Requested effective module visibility for class : ${this.qualifiedName?.asString()} with unexpected visibility : $visibility")
    }

    return localVisibility
}
