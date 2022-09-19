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
package com.jayasuryat.dowel.annotation

/**
 * Only classes extending [androidx.compose.ui.tooling.preview.PreviewParameterProvider] can be
 * annotated with this annotation.
 *
 * This annotation tells the processor to *consider* this implementation of [androidx.compose.ui.tooling.preview.PreviewParameterProvider]
 * while generating PreviewParameterProviders for other classes.
 *
 * This comes in handy when a [Dowel] annotated class has properties of types which are not directly
 * supported by Dowel. In such cases Dowel processor will get instances of those unsupported types from the annotated implementation
 * of [androidx.compose.ui.tooling.preview.PreviewParameterProvider].
 *
 * Or in certain cases where random values are not really suitable for particular a type, then an
 * implementation of [androidx.compose.ui.tooling.preview.PreviewParameterProvider] can fill in that
 * information using this annotation.
 *
 * In all of the generated code wherever the type of a property matches with the type of
 * [androidx.compose.ui.tooling.preview.PreviewParameterProvider] - instances will be retrieved from
 * an instance of that provider for that type, instead of building instances by *hand*.
 *
 * **Note** : Only a single class can be annotated with @[ConsiderForDowel] per type.
 *
 * @see [Dowel]
 */
@Suppress("KDocUnresolvedReference")
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class ConsiderForDowel
