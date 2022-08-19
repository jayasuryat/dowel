package com.jayasuryat.dowel.sample.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {

        Text("Home")
    }
}

@Preview(
    name = "Home screen",
    showBackground = true,
)
@Composable
private fun Preview() {
    HomeScreen()
}