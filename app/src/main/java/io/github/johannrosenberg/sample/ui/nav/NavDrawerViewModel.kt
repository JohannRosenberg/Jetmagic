package io.github.johannrosenberg.sample.ui.nav

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs

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

