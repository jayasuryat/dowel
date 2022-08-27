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
@file:Suppress("SpellCheckingInspection")

package com.jayasuryat.dowel.processor

internal object StringSource {

    private const val source: String =
        "Lorem ipsum dolor sit amet consectetur adipiscing elit Donec dui ante suscipit sed neque ut rhoncus condimentum nunc Phasellus malesuada congue magna non congue In commodo massa ac purus lobortis scelerisque Proin a sapien consequat bibendum sapien nec mollis turpis Cras est nisi accumsan et mattis eu tempus ac purus Proin ultricies metus et libero malesuada posuere Duis est metus bibendum sed ultrices quis luctus in ex Sed a lectus at metus iaculis euismod Praesent pretium hendrerit lacus a tincidunt mi aliquet malesuada Duis congue finibus nisi vel dapibus Nam imperdiet porttitor enim et lacinia"

    private val words: List<String> = source
        .split(" ")
        .map { word -> word.trim() }

    internal fun getRandomWord(): String = words.random()
}
