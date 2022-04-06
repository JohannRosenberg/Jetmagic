package dev.wirespec.sample.ui.screens.petdetails.de

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.sample.ui.theme.AppTheme

@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {
    PetDetailsUI()
}

@Composable
fun PetDetailsUI() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            // Important: verticalScroll must be before padding to prevent click events along the padded edge
            // from propagating to the screen below.
            .background(AppTheme.appColorTheme.materialColors.surface)
            .verticalScroll(rememberScrollState())
            .padding(40.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "If you're seeing this, it means that your device is set to using German as the default language. " +
                    "To see the actual pet details screen, switch your device to a different language."
        )
    }
}