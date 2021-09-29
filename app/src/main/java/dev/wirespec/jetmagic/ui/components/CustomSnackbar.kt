package dev.wirespec.jetmagic.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.wirespec.jetmagic.ui.screens.main.MainViewModel
import dev.wirespec.jetmagic.ui.theme.AppTheme
import dev.wirespec.jetmagic.ui.theme.shapes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun CustomSnackbarHandler(modifier: Modifier = Modifier) {

    val vmMain: MainViewModel = viewModel()
    val visible = vmMain.onSnackbarVisible.observeAsState(false)

    CustomSnackbar(
        visible = visible.value,
        snackbarInfo = vmMain.snackbarInfo,
        modifier = modifier,
        onTimeout = {
            vmMain.hideSnackbar()
        }) {
        vmMain.onSnackbarActionButtonClick()
    }
}


@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@Composable
fun CustomSnackbar(
    visible: Boolean,
    snackbarInfo: SnackbarInfo,
    modifier: Modifier = Modifier,
    onTimeout: () -> Unit,
    onActionButtonClick: () -> Unit
) {

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            shape = shapes.medium,
            color = (if (snackbarInfo.isCritical)
                AppTheme.appColorTheme.snackbarCriticalBackground
            else
                AppTheme.appColorTheme.snackbarNormalBackground),
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                if (snackbarInfo.showProgressIndicator) {
                    Box(modifier = Modifier.padding(start = 15.dp, top = 15.dp, bottom = 15.dp)) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .requiredWidth(20.dp)
                                .requiredHeight(20.dp),
                            color = AppTheme.appColorTheme.snackbarNormalText,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Box(modifier = modifier.weight(if (snackbarInfo.actionCallback != null) 3f else 1f)) {
                    Text(
                        snackbarInfo.message,
                        modifier = modifier.padding(15.dp),
                        color = if (snackbarInfo.isCritical) AppTheme.appColorTheme.snackbarCriticalText else AppTheme.appColorTheme.snackbarNormalText
                    )
                }

                var job: Job? = null

                if (snackbarInfo.actionCallback != null) {
                    Box(modifier = modifier
                        .weight(1f)
                        .clickable {
                            job?.cancel()
                            onActionButtonClick()
                        }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            snackbarInfo.actionLabel,
                            color = if (snackbarInfo.isCritical) AppTheme.appColorTheme.snackbarCriticalAction else AppTheme.appColorTheme.snackbarNormalAction,
                            modifier = modifier
                                .padding(15.dp)

                        )
                    }
                }

                val composableScope = rememberCoroutineScope()

                job = composableScope.launch {
                    delay(4000)
                    onTimeout()
                }
            }
        }
    }
}