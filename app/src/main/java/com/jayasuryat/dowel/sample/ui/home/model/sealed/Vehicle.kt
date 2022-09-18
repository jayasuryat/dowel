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
package com.jayasuryat.dowel.sample.ui.home.model.sealed

import com.jayasuryat.dowel.annotation.Dowel

sealed interface Vehicle {

    object Dummy : Vehicle

    @Dowel
    data class Bike(
        val make: String,
        val model: Int,
    ) : Vehicle

    @Dowel
    data class Car(
        val make: String,
        val model: Int,
    ) : Vehicle

    sealed interface FourWheeler : Vehicle {

        @Dowel
        data class Car(
            val make: String,
            val model: Int,
        ) : FourWheeler

        @Dowel
        data class Truck(
            val make: String,
            val model: Int,
            val bedCapacity: Float,
        ) : FourWheeler
    }
}
