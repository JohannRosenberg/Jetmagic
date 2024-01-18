package io.github.johannrosenberg.sample.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.johannrosenberg.sample.ui.screens.main.MainViewModel
import io.github.johannrosenberg.sample.ui.theme.AppColors
import io.github.johannrosenberg.sample.ui.theme.shapes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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

@Composable
fun CustomSnackbar(
    visible: Boolean,
    snackbarInfo: SnackbarInfo,
    modifier: Modifier = Modifier,
    onTimeout: () -> Unit,
    onActionButtonClick: () -> Unit
) {
    var job: Job? = null
    val composableScope = rememberCoroutineScope()

    LaunchedEffect(visible) {
        job = composableScope.launch {
            delay(4000)
            onTimeout()
        }
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }),
        exit = slideOutVertically(targetOffsetY = { it })
    ) {
        Surface(
            shape = shapes.medium,
            color = (if (snackbarInfo.isCritical)
             AppColors.snackbarCriticalBg
            else
               AppColors.snackbarNormalBg),
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
                            color = AppColors.snackbarNormalText,
                            strokeWidth = 2.dp
                        )
                    }
                }

                Box(modifier = modifier.weight(if (snackbarInfo.actionCallback != null) 3f else 1f)) {
                    Text(
                        snackbarInfo.message,
                        modifier = modifier.padding(15.dp),
                        color = if (snackbarInfo.isCritical) AppColors.snackbarCriticalText else AppColors.snackbarNormalText
                    )
                }



                if (snackbarInfo.actionCallback != null) {
                    Box(
                        modifier = modifier
                            .weight(1f)
                            .clickable {
                                job?.cancel()
                                onActionButtonClick()
                            }, contentAlignment = Alignment.Center
                    ) {
                        Text(
                            snackbarInfo.actionLabel,
                            color = if (snackbarInfo.isCritical) AppColors.snackbarCriticalAction else AppColors.snackbarNormalAction,
                            modifier = modifier
                                .padding(15.dp)

                        )
                    }
                }
            }
        }
    }
}