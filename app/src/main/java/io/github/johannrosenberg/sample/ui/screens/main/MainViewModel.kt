package io.github.johannrosenberg.sample.ui.screens.main

import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.johannrosenberg.sample.ui.components.SnackbarInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    var drawerState: DrawerState = DrawerState(initialValue = DrawerValue.Closed)
    var snackbarInfo = SnackbarInfo()

    private var _onSnackbarVisible = MutableLiveData(false)
    var onSnackbarVisible: LiveData<Boolean> = _onSnackbarVisible

    fun displaySnackbar(sbInfo: SnackbarInfo) {
        viewModelScope.launch(context = Dispatchers.Main) {
            snackbarInfo = sbInfo
            _onSnackbarVisible.value = true
        }
    }

    fun onSnackbarActionButtonClick() {
        _onSnackbarVisible.value = false

        viewModelScope.launch {
            delay(200)
            snackbarInfo.actionCallback?.invoke()
        }
    }

    fun hideSnackbar() {
        _onSnackbarVisible.value = false
    }
}