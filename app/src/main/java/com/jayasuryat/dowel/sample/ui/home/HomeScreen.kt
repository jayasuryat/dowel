package com.jayasuryat.dowel.sample.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp

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
                        .wrapContentHeight(),
                    person = person
                )
            }
        }
    }
}

@Composable
private fun ItemPerson(
    modifier: Modifier = Modifier,
    person: Person,
) {

    Row(
        modifier = modifier
            .padding(8.dp)
            .clip(RoundedCornerShape(100f))
            .background(color = Color.Gray.copy(alpha = 0.3f))
            .padding(all = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {

        Image(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color = Color.Gray.copy(alpha = 0.8f)),
            painter = painterResource(id = person.avatar),
            contentDescription = "Person avatar",
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
            text = person.age.toString(),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(
    name = "Item person",
    showBackground = true,
)
@Composable
private fun Preview(
    @PreviewParameter(PersonPreviewParamProvider::class) person: Person,
) {
    ItemPerson(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        person = person,
    )
}
