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

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jayasuryat.dowel.sample.ui.home.model.Person

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    people: List<Person>,
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {

            items(
                items = people,
                key = { person -> person.name },
            ) { person ->

                ItemPerson(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                        .padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    person = person
                )
            }
        }
    }
}
