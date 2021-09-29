package dev.wirespec.jetmagic.ui.screens.main

import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ScaffoldState
import androidx.compose.material.SnackbarHostState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.wirespec.jetmagic.ui.components.SnackbarInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    var scaffoldState: ScaffoldState = ScaffoldState(DrawerState(initialValue = DrawerValue.Closed), SnackbarHostState())
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