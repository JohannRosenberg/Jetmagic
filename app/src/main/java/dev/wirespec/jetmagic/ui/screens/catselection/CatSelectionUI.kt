package dev.wirespec.jetmagic.ui.screens.catselection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableParams
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.components.BasicRadioButton
import dev.wirespec.jetmagic.ui.theme.AppTheme

@Composable
fun CatSelectionHandler(composableInstance: ComposableInstance) {

    val p = composableInstance.parameters as ComposableParams?

    CatSelection(
        onSelection = { selectedBreed ->
            p?.onReturn?.invoke(selectedBreed, false)
            navman.goBack()
        })
}

@Composable
fun CatSelection(
    modifier: Modifier = Modifier,
    onSelection: (catBreedSelected: String) -> Unit
) {
    var selectedBreed by remember { mutableStateOf("Lion") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(10.dp)
            .fillMaxSize()
            .background(AppTheme.appColorTheme.materialColors.surface)
            .verticalScroll(rememberScrollState())
    ) {
        Column(modifier = modifier.padding(bottom = 30.dp)) {
            BasicRadioButton(
                id = "lion",
                text = "Lion",
                selected = selectedBreed == "Lion"
            ) { id, text ->
                selectedBreed = text
            }

            BasicRadioButton(
                id = "jaguar",
                text = "Jaguar",
                selected = selectedBreed == "Jaguar"
            ) { id, text ->
                selectedBreed = text
            }

            BasicRadioButton(
                id = "tiger",
                text = "Tiger",
                selected = selectedBreed == "Tiger"
            ) { id, text ->
                selectedBreed = text
            }
        }

        Button(
            modifier = modifier.padding(bottom = 10.dp),
            colors = AppTheme.getButtonColors(),
            elevation = ButtonDefaults.elevation(5.dp),
            onClick = {
                onSelection(selectedBreed)
            }) {
            Text(
                text = "Return selection",
                modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
            )
        }
    }
}


