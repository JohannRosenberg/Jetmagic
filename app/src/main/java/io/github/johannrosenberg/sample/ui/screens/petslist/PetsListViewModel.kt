package io.github.johannrosenberg.sample.ui.screens.petslist

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import io.github.johannrosenberg.jetmagic.composables.IImageManager
import io.github.johannrosenberg.jetmagic.composables.ImageManager
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.da.web.PetAPIOptions
import io.github.johannrosenberg.sample.models.PetDetailsParams
import io.github.johannrosenberg.sample.models.PetListItemInfo
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoilApi::class)
class PetsListViewModel : ViewModel(), IImageManager {

    var scrollState: ScrollState = ScrollState(0)
    private var lastSelectedPetInfo: PetListItemInfo? = null
    private val imageMan = ImageManager()

    override val imageManager: ImageManager
        get() = imageMan

    private var _onPetsAvailable = MutableLiveData<List<PetListItemInfo>>(null)
    var onPetsAvailable: LiveData<List<PetListItemInfo>> = _onPetsAvailable

    init {
        val pets = io.github.johannrosenberg.sample.da.Repository.pets

        if (pets == null) {
            viewModelScope.launch {
                val petsList = io.github.johannrosenberg.sample.da.Repository.getPets(PetAPIOptions())
                _onPetsAvailable.value = petsList
            }
        } else {
            _onPetsAvailable.value = pets!!
        }
    }

    private fun updatePetDetails(composableInstance: ComposableInstance, petInfo: PetListItemInfo) {
        val p = composableInstance.parameters as PetDetailsParams
        p.petsListItemInfo = petInfo
        lastSelectedPetInfo = petInfo


        crm.notifyChildComposableInstanceOfUpdate(
            parentComposableInstance = composableInstance,
            childComposableResourceId = ComposableResourceIDs.PetDetails
        )
    }

    fun updatePetDetailsIfPresent(composableInstance: ComposableInstance, petInfo: PetListItemInfo) {
        val petDetailsComposableInstance = crm.getChildComposableInstance(
            parentComposableInstance = composableInstance,
            childComposableResourceId = ComposableResourceIDs.PetDetails,
        )

        if (petDetailsComposableInstance != null) {
            if (lastSelectedPetInfo == null) {
                updatePetDetails(composableInstance=  petDetailsComposableInstance, petInfo = petInfo)
                lastSelectedPetInfo = petInfo
            } else {
                updatePetDetails(composableInstance=  petDetailsComposableInstance, petInfo = lastSelectedPetInfo!!)
            }
        }
    }

    fun updateOrGotoPetDetails(composableInstance: ComposableInstance, petInfo: PetListItemInfo) {
        lastSelectedPetInfo = petInfo

        val petDetailsComposableInstance = crm.getChildComposableInstance(
            parentComposableInstance = composableInstance,
            childComposableResourceId = ComposableResourceIDs.PetDetails,
        )

        if (petDetailsComposableInstance == null) {
            val petDetailsParams = PetDetailsParams(petsListItemInfo = petInfo)
            navman.goto(composableResId = ComposableResourceIDs.PetDetailsScreen, p = petDetailsParams)
        } else {
            updatePetDetails(composableInstance=  petDetailsComposableInstance, petInfo = petInfo)
        }
    }
}