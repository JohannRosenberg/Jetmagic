package dev.wirespec.jetmagic.ui.nav

import androidx.compose.foundation.ScrollState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@ExperimentalMaterialApi
class NavDrawerViewModel : ViewModel() {

    var navDrawerScrollState: ScrollState = ScrollState(0)

    private val _currentMenuId = MutableLiveData(NavMenuConstants.MenuHome)
    val currentMenuId: LiveData<String> = _currentMenuId

    init {
        navman.observeScreenChange {
            if (navman.totalScreensDisplayed == 1) {
                _currentMenuId.value = NavMenuConstants.MenuHome
            }
        }
    }

    fun onNavItemClick(menuId: String, composableResId: String, p: Any? = null) {
        _currentMenuId.value = menuId

        if (composableResId == ComposableResourceIDs.PetsList) {
            navman.gotoHomeScreen()
        } else {
            navman.goto(composableResId = composableResId, p = p)
        }
    }
}

