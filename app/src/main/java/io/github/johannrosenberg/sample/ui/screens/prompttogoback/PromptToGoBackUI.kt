package io.github.johannrosenberg.sample.ui.screens.prompttogoback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman

@Composable
fun PromptToGoBackHandler(composableInstance: ComposableInstance) {
    PromptToGoBack()
}

@Composable
fun PromptToGoBack(modifier: Modifier = Modifier) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(10.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            modifier = modifier.padding(bottom = 10.dp),
            //colors = AppTheme.getButtonColors(),
            elevation = ButtonDefaults.buttonElevation(5.dp),
            onClick = {
                navman.goBack()
            }) {
            Text(
                text = "Return to previous screen",
                modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
            )
        }
    }
}




