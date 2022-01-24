package dev.wirespec.jetmagic.ui.screens.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.wirespec.jetmagic.App
import dev.wirespec.jetmagic.R
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableParams
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs
import dev.wirespec.jetmagic.ui.components.SnackbarInfo
import dev.wirespec.jetmagic.ui.screens.ScreenGlobals
import dev.wirespec.jetmagic.ui.theme.AppColors
import dev.wirespec.jetmagic.ui.theme.AppTheme

@Composable
fun TestHandler(composableInstance: ComposableInstance) {

    val vm = composableInstance.viewmodel as TestViewModel
    var screenText = composableInstance.parameters as String?

    if (screenText == null) {
        screenText = ""
    }

    Test(
        screenId = vm.screenId,
        screenText = screenText,
        onBackButtonClick = {
            navman.goBack()
        })
}

@Composable
fun Test(
    screenId: Int,
    screenText: String,
    modifier: Modifier = Modifier,
    onBackButtonClick: () -> Unit
) {
    val locale = LocalConfiguration.current.locale

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.appColorTheme.materialColors.surface)
    ) {

        TopAppBar(
            modifier = Modifier.height(ScreenGlobals.DefaultToolbarHeight),
            elevation = 0.dp,
            title = {
                Text(screenText, color = AppTheme.appColorTheme.materialColors.secondary)
            },
            navigationIcon = {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        modifier = modifier
                            .requiredWidth(ScreenGlobals.ToolbarBackButtonIconSize)
                            .requiredHeight(ScreenGlobals.ToolbarBackButtonIconSize),
                        tint = AppColors.turquoise,
                        imageVector = Icons.Filled.ArrowLeft,
                        contentDescription = ""
                    )
                }
            }
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier
                .fillMaxSize()
                // Important: verticalScroll must be before padding to prevent click events along the padded edge
                // from propagating to the screen below.
                .verticalScroll(rememberScrollState())
                .padding(10.dp)

        ) {
            Text(
                "Screen id...$screenId",
                fontSize = 15.sp,
                modifier = modifier.padding(bottom = 10.dp)
            )

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    navman.goto(composableResId = ComposableResourceIDs.TestScreen, p = "Test Screen")
                }) {
                Text(
                    text = App.context.getString(R.string.go_to_another_screen),
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    navman.gotoHomeScreen()
                }) {
                Text(
                    text = App.context.getString(R.string.go_to_home_screen),
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    val p = ComposableParams() { result, canceled ->
                        App.mainViewModel.displaySnackbar(SnackbarInfo(message = "You selected: $result"))
                    }

                    navman.goto(composableResId = ComposableResourceIDs.CatSelectionScreen, p = p)

                }) {
                Text(
                    text = App.context.getString(R.string.return_value_from_another_screen),
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    navman.goto(composableResId = ComposableResourceIDs.PromptToGoBackScreen)
                }) {
                Text(
                    text = App.context.getString(R.string.prompt_when_returning),
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    App.context.currentActivity?.finish()
                }) {
                Text(
                    text = App.context.getString(R.string.terminate_activity),
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }

            Button(
                modifier = modifier.padding(bottom = 10.dp),
                colors = AppTheme.getButtonColors(),
                elevation = ButtonDefaults.elevation(5.dp),
                onClick = {
                    App.context.setAppLocale("de")
                }) {
                Text(
                    text = "Change UI to German",
                    modifier = modifier.padding(start = 10.dp, top = 7.dp, end = 10.dp, bottom = 7.dp)
                )
            }
        }
    }
}