package com.jayasuryat.dowel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.jayasuryat.dowel.sample.ui.home.HomeScreen
import com.jayasuryat.dowel.sample.ui.home.Person
import com.jayasuryat.dowel.sample.ui.home.PersonPreviewParamProvider
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
