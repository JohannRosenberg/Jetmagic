package dev.wirespec.jetmagic.ui.screens.prompttogoback

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.theme.AppTheme


@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PromptToGoBackHandler(composableInstance: ComposableInstance) {
    PromptToGoBack()
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PromptToGoBack(modifier: Modifier = Modifier) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(10.dp)
            .fillMaxSize()
            .background(AppTheme.appColorTheme.materialColors.surface)
            .verticalScroll(rememberScrollState())
    ) {
        Button(
            modifier = modifier.padding(bottom = 10.dp),
            colors = AppTheme.getButtonColors(),
            elevation = ButtonDefaults.elevation(5.dp),
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




