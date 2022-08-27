package com.jayasuryat.dowel.sample.ui.home

import androidx.annotation.DrawableRes
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.sample.R

@Dowel
data class Person(
    val name: String,
    val age: Int,
    @DrawableRes val avatar: Int = R.drawable.ic_launcher_foreground,
    val count: Long,
    val status: Status,
    val weight: Float,
    val height: Double,
    val isAlien: Boolean,
    val title: Char,
    val onClick: suspend (a: Person, b: Int) -> Unit,
)

enum class Status {
    Active, InActive, PendingActivation, Deleted, Archived
}