package dev.wirespec.jetmagic.ui.screens.petslist

import androidx.compose.foundation.ScrollState
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.annotation.ExperimentalCoilApi
import dev.wirespec.jetmagic.composables.IImageManager
import dev.wirespec.jetmagic.composables.ImageManager
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.da.Repository
import dev.wirespec.jetmagic.da.web.PetAPIOptions
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.PetDetailsParams
import dev.wirespec.jetmagic.models.PetListItemInfo
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs
import kotlinx.coroutines.launch

@ExperimentalCoilApi
class PetsListViewModel : ViewModel(), IImageManager {

    var scrollState: ScrollState = ScrollState(0)
    private var lastSelectedPetInfo: PetListItemInfo? = null
    private val imageMan = ImageManager()

    override val imageManager: ImageManager
        get() = imageMan

    private var _onPetsAvailable = MutableLiveData<List<PetListItemInfo>>(null)
    var onPetsAvailable: LiveData<List<PetListItemInfo>> = _onPetsAvailable

    init {
        val pets = Repository.pets

        if (pets == null) {
            viewModelScope.launch {
                val petsList = Repository.getPets(PetAPIOptions())
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