package io.github.johannrosenberg.sample.ui.screens.petdetails

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import io.github.johannrosenberg.jetmagic.composables.IImageManager
import io.github.johannrosenberg.jetmagic.composables.ImageManager
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.models.PetListItemInfo
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
class PetDetailsViewModel: ViewModel(), IImageManager {

    var screenScrollState: ScrollState = ScrollState(0)
    var petInfo: PetListItemInfo? = null

    private val imageMan = ImageManager()

    override val imageManager: ImageManager
        get() = imageMan

    fun processDeepLink(composableInstance: ComposableInstance) {
        val deepLink = navman.getDeepLinkForComposableInstance(composableInstance = composableInstance)

        if (deepLink != null) {
            val queryParamName = deepLink.queryKeyValues["name"]

            if (queryParamName.isNullOrEmpty()) {
                return
            }

            viewModelScope.launch {
                petInfo = io.github.johannrosenberg.sample.da.Repository.getPetByName(queryParamName)
                crm.notifyComposableInstanceOfUpdate(composableInstance = composableInstance)
            }

            // Since we don't support any further deep links beyond the details screen,
            // clear any additional deep links that might still be on the stack.
            navman.clearDeepLinks()
        }
    }
}