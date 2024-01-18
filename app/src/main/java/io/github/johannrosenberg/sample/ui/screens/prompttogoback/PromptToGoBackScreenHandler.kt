package io.github.johannrosenberg.sample.ui.screens.prompttogoback

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs

@Composable
fun PromptToGoBackScreenHandler(composableInstance: ComposableInstance) {
    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderChildComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.PromptToGoBack,
            childComposableId = "promptToGoBack",
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