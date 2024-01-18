package io.github.johannrosenberg.sample.ui.screens.main

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.johannrosenberg.jetmagic.App
import io.github.johannrosenberg.jetmagic.composables.ScreenFactoryHandler
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.ui.components.CustomSnackbarHandler
import io.github.johannrosenberg.sample.ui.nav.NavDrawerHandler

@Composable
fun MainHandler(modifier: Modifier = Modifier) {
    App.mainViewModel = viewModel()
    val scaffoldState = App.mainViewModel.drawerState

    var drawerGesturesEnabled by remember { mutableStateOf(true) }

    navman.observeScreenChange {
        drawerGesturesEnabled = (navman.totalScreensDisplayed == 1)
    }

    Main(scaffoldState, drawerGesturesEnabled = drawerGesturesEnabled, modifier = modifier)
}

@OptIn(ExperimentalAnimationApi::class)
@SuppressLint("UnusedMaterialScaffoldPaddingParameter", "UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun Main(drawerState: DrawerState, drawerGesturesEnabled: Boolean, modifier: Modifier = Modifier) {
    Box {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(drawerContainerColor = Color.Transparent) {
                    NavDrawerHandler(drawerState = drawerState)
                }
            },
            gesturesEnabled = drawerGesturesEnabled,
            content = {
                Scaffold(
                    content = {
                        ScreenFactoryHandler()
                    }
                )
            }
        )

        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
            CustomSnackbarHandler(modifier = modifier)
        }
    }
}

