package dev.wirespec.jetmagic.ui.screens.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Scaffold
import androidx.compose.material.ScaffoldState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.wirespec.jetmagic.App
import dev.wirespec.jetmagic.composables.ScreenFactoryHandler
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.components.CustomSnackbarHandler
import dev.wirespec.jetmagic.ui.nav.NavDrawerHandler

@Composable
fun MainHandler(modifier: Modifier = Modifier) {
    App.mainViewModel = viewModel()
    val scaffoldState = App.mainViewModel.scaffoldState

    var drawerGesturesEnabled by remember { mutableStateOf(true) }

    navman.observeScreenChange {
        drawerGesturesEnabled = (navman.totalScreensDisplayed == 1)
    }

    Main(scaffoldState, drawerGesturesEnabled = drawerGesturesEnabled, modifier = modifier)
}

@Composable
fun Main(scaffoldState: ScaffoldState, drawerGesturesEnabled: Boolean, modifier: Modifier = Modifier) {
    Box {
        Scaffold(
            modifier = modifier,
            drawerGesturesEnabled = drawerGesturesEnabled,
            scaffoldState = scaffoldState,
            drawerBackgroundColor = Color.Transparent,
            drawerElevation = 0.dp,
            drawerContent = {
                NavDrawerHandler(scaffoldState = scaffoldState)
            },
            content = {
                ScreenFactoryHandler()
            }
        )

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            CustomSnackbarHandler(modifier = modifier)
        }
    }
}

