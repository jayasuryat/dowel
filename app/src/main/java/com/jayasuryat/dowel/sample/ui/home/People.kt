package com.jayasuryat.dowel.sample.ui.home

import kotlin.math.absoluteValue
import kotlin.random.Random

private fun randomAge(): Int = Random.nextInt(100).absoluteValue

internal val People: List<Person> = listOf(
    Person(
        name = "Eula Austin",
        age = randomAge(),
    ),
    Person(
        name = "Jeanne Sharp",
        age = randomAge(),
    ),
    Person(
        name = "Clinton Fowler",
        age = randomAge(),
    ),
    Person(
        name = "Joanna Coleman",
        age = randomAge(),
    ),
    Person(
        name = "Brandi Floyd",
        age = randomAge(),
    )
)
