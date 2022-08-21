package com.jayasuryat.dowel.sample.ui.home

import com.jayasuryat.dowel.annotation.Dowel

@Dowel
data class Person(
    val name: String,
    val age: Int,
    val count: Long,
    val weight: Float,
    val height: Double,
    val isAlien: Boolean,
    val title: Char,
)
