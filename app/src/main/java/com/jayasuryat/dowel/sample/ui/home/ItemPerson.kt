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

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jayasuryat.dowel.sample.ui.home.model.Person
import com.jayasuryat.dowel.sample.ui.home.model.PersonPreviewParamProvider
import com.jayasuryat.dowel.sample.ui.home.model.location.Location
import com.jayasuryat.dowel.sample.ui.home.model.location.LocationCustomPreviewParamProvider

@Composable
internal fun ItemPerson(
    modifier: Modifier = Modifier,
    person: Person,
) {

    val liveLocation: State<Location?> = person.liveLocation.collectAsState(null)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(color = Color.Gray.copy(alpha = 0.2f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize()
                .clip(RoundedCornerShape(100f))
                .background(color = Color.Gray.copy(alpha = 0.3f)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Image(
                modifier = Modifier
                    .padding(8.dp)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray.copy(alpha = 0.8f)),
                painter = painterResource(id = person.avatar),
                contentDescription = "Person avatar",
            )

            Text(
                modifier = Modifier.wrapContentSize(),
                text = person.title.uppercase(),
            )

            Text(
                modifier = Modifier.weight(1f),
                text = person.name,
            )

            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .clip(CircleShape)
                    .background(color = Color.Gray.copy(alpha = 0.8f))
                    .padding(8.dp),
                text = person.status.toString(),
                textAlign = TextAlign.Center,
            )

            Text(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(8.dp)
                    .clip(CircleShape)
                    .background(color = Color.Gray.copy(alpha = 0.8f))
                    .padding(8.dp),
                text = person.age.toString(),
                textAlign = TextAlign.Center,
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(32.dp))
                .background(color = Color.Gray.copy(alpha = 0.3f))
                .padding(32.dp),
        ) {

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                text = person.bio ?: "--",
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(100f))
                        .background(color = Color.Gray.copy(alpha = 0.8f))
                        .padding(8.dp),
                    fontSize = 12.sp,
                    text = "Height : ${person.height.format(2)}",
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(100f))
                        .background(color = Color.Gray.copy(alpha = 0.8f))
                        .padding(8.dp),
                    fontSize = 12.sp,
                    text = "Weight : ${person.weight.format(2)}",
                    textAlign = TextAlign.Center,
                )

                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .clip(RoundedCornerShape(100f))
                        .background(color = Color.Gray.copy(alpha = 0.8f))
                        .padding(8.dp),
                    fontSize = 12.sp,
                    text = "Likes : ${person.likes}",
                    textAlign = TextAlign.Center,
                )
            }
        }

        LocationItem(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            title = "Location",
            location = person.location,
        )

        LocationItem(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            title = "Live Location",
            location = liveLocation.value ?: Location(lat = 0, lon = 1),
        )
    }
}

@Composable
private fun LocationItem(
    modifier: Modifier = Modifier,
    title: String,
    location: Location,
) {

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(100f))
            .background(color = Color.Gray.copy(alpha = 0.3f)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Image(
            modifier = Modifier
                .padding(8.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(color = Color.Gray.copy(alpha = 0.8f))
                .padding(8.dp),
            painter = rememberVectorPainter(Icons.Filled.LocationOn),
            contentDescription = "Location",
        )

        Text(
            modifier = Modifier.wrapContentSize(),
            text = title,
        )

        Text(
            modifier = Modifier.wrapContentSize(),
            text = "${location.lat}, ${location.lon}",
        )
    }
}

private fun Float.format(digits: Int) = "%.${digits}f".format(this)
private fun Double.format(digits: Int) = "%.${digits}f".format(this)

@Preview(
    name = "Item person",
    showBackground = true,
)
@Composable
private fun PersonPreview(
    @PreviewParameter(PersonPreviewParamProvider::class) person: Person,
) {
    ItemPerson(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        person = person,
    )
}

@Preview(
    name = "Location item",
    showBackground = true,
)
@Composable
private fun LocationPreview(
    @PreviewParameter(LocationCustomPreviewParamProvider::class) location: Location,
) {
    LocationItem(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        title = "Location",
        location = location,
    )
}
