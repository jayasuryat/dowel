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
package com.jayasuryat.dowel.sample.ui.home

import androidx.annotation.DrawableRes
import androidx.annotation.Size
import androidx.compose.runtime.State
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.sample.R

@Dowel
data class Person(
    @Size(value = 5) val name: String,
    val age: Int,
    @DrawableRes val avatar: Int = R.drawable.ic_launcher_foreground,
    val count: Long,
    val status: Status,
    val weight: Float,
    val location: Location,
    @Size(value = 2) val locations: List<Location>,
    val isExpanded: State<Boolean>,
    val height: Double,
    val isAlien: Boolean,
    val title: Char,
    @Size(value = 1) val interests: List<Float>,
    val onClick: suspend (a: Person, b: Int) -> Unit,
)

@Dowel
data class Location(
    val lat: Long,
    val lon: Long,
)

enum class Status {
    Active, InActive, PendingActivation, Deleted, Archived
}
