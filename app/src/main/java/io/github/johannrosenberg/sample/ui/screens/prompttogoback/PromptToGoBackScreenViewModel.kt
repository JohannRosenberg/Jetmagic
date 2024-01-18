package io.github.johannrosenberg.sample.ui.screens.prompttogoback

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.johannrosenberg.jetmagic.navigation.NavigateBackOptions
import io.github.johannrosenberg.jetmagic.navigation.NavigationManagerHelper
import io.github.johannrosenberg.jetmagic.navigation.navman

class PromptToGoBackScreenViewModel : ViewModel(), NavigationManagerHelper {

    private val _onDisplayDialog = MutableLiveData(false)
    val onDisplayDialog: LiveData<Boolean> = _onDisplayDialog

    fun onDialogResponse(confirmed: Boolean) {
        _onDisplayDialog.value = false

        if (confirmed) {
            navman.goBackImmediately()
        }
    }

    override fun onNavigateBack(): NavigateBackOptions? {
        _onDisplayDialog.value = true
        return NavigateBackOptions.Cancel
    }
}