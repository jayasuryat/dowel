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
package com.jayasuryat.dowel.sample.ui.home.model

import androidx.annotation.DrawableRes
import androidx.annotation.Size
import androidx.compose.runtime.State
import com.jayasuryat.dowel.annotation.Dowel
import com.jayasuryat.dowel.annotation.DowelList
import com.jayasuryat.dowel.sample.R
import com.jayasuryat.dowel.sample.ui.home.model.location.Location
import com.jayasuryat.dowel.sample.ui.home.model.meta.SomeStaticInfo
import com.jayasuryat.dowel.sample.ui.home.model.sealed.Vehicle
import com.jayasuryat.dowel.sample.ui.home.model.status.Status
import com.jayasuryat.dowel.sample.ui.home.model.unsupported.UnsupportedType
import kotlinx.coroutines.flow.Flow

@DowelList(count = 5)
@Dowel(count = 3)
data class Person(
    @Size(value = 5) val name: String,
    @Size(value = 300) val bio: String?,
    @DrawableRes val avatar: Int = R.drawable.ic_launcher_foreground,
    val age: Int,
    val status: Status,
    val weight: Float,
    val height: Double,
    val likes: Long,
    val isAlien: Boolean,
    val location: Location,
    val vehicle: Vehicle,
    val liveLocation: Flow<Location?>,
    val latLon: Pair<Long, Long>,
    val meta: SomeStaticInfo,
    val customType: UnsupportedType = UnsupportedType.SomeType,
    @Size(value = 2) val locations: List<Location>,
    val isExpanded: State<Boolean>,
    @Size(value = 1) val preferences: Map<Long, Location>,
    val title: Char,
    @Size(value = 1) val interests: List<Float>,
    val onClick: suspend (a: Person, b: Int) -> Unit,
)
