package dev.wirespec.jetmagic.ui.screens.prompttogoback

import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@Composable
fun PromptToGoBackScreenHandler(composableInstance: ComposableInstance) {
    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderChildComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.PromptToGoBack,
            childComposableId = "promptToGoBack"
        )

        DialogToReturnHandler()
    }
}

@Composable
fun DialogToReturnHandler() {
    val composableInstance = LocalComposableInstance.current
    val vm = composableInstance.viewmodel as PromptToGoBackScreenViewModel

    val displayDialog = vm.onDisplayDialog.observeAsState().value

    DialogToReturn(
        visible = displayDialog as Boolean,
        onClickResponse = {confirmed ->
            vm.onDialogResponse(confirmed = confirmed)
        })
}

@Composable
fun DialogToReturn(
    visible: Boolean = false,
    onClickResponse: (confirmed: Boolean) -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = {

            },
            title = { Text(text = "Action") },
            text = { Text("Do you want to return to the previous screen?") },
            confirmButton = {
                Button(
                    onClick = { onClickResponse(true) }) {
                    Text("Yes")
                }
            },
            dismissButton = {
                Button(
                    onClick = { onClickResponse(false) }) {
                    Text("No")
                }
            }
        )
    }
}