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
package com.jayasuryat.dowel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.jayasuryat.dowel.sample.ui.home.HomeScreen
import com.jayasuryat.dowel.sample.ui.home.model.Person
import com.jayasuryat.dowel.sample.ui.home.model.PersonPreviewParamProvider
import com.jayasuryat.dowel.sample.ui.theme.DowelTheme

class MainActivity : ComponentActivity() {

    private val people: List<Person> by lazy { getRandomPeople() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            DowelTheme {
                HomeScreen(
                    modifier = Modifier.fillMaxSize(),
                    people = people,
                )
            }
        }
    }

    private fun getRandomPeople(): List<Person> {
        return PersonPreviewParamProvider().values.toList()
    }
}
