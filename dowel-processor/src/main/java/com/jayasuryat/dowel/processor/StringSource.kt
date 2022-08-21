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