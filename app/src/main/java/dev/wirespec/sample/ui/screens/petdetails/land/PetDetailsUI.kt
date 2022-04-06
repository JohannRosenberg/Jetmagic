package dev.wirespec.sample.ui.screens.petdetails.land

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.sample.models.PetDetailsParams
import dev.wirespec.sample.models.PetListItemInfo
import dev.wirespec.sample.ui.ComposableResourceIDs
import dev.wirespec.sample.ui.screens.ScreenGlobals
import dev.wirespec.sample.ui.screens.petdetails.PetDetailStatsUI
import dev.wirespec.sample.ui.screens.petdetails.PetDetailsViewModel
import dev.wirespec.sample.ui.screens.petdetails.PetImageGalleryHandler
import dev.wirespec.sample.ui.theme.AppColors
import dev.wirespec.sample.ui.theme.AppTheme
import dev.wirespec.sample.ui.theme.MaterialColors

@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    // Get notified of updates.
    composableInstance.onUpdate?.observeAsState()?.value

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        val vm = composableInstance.viewmodel as PetDetailsViewModel
        vm.processDeepLink(composableInstance = composableInstance)

        val p = composableInstance.parameters as PetDetailsParams?
        val modifier: Modifier = p?.modifier ?: Modifier
        val displayAppBar: Boolean = p?.displayAppBar ?: true
        var pet = p?.petsListItemInfo

        if ((pet == null) && (vm.petInfo != null)) {
            pet = vm.petInfo
        }

        if ((pet != null) && ((vm.petInfo == null) || (pet.id != vm.petInfo?.id))) {
            vm.petInfo = pet
            vm.imageManager.onComposableInstanceTerminated(composableInstance = composableInstance)
            vm.imageManager.clearAllImageStates()
        }

        PetDetailsUI(
            modifier = modifier,
            pet = pet,
            scrollState = vm.screenScrollState,
            onAdoptClick = {
                navman.goto(composableResId = ComposableResourceIDs.TestScreen)
            },
            onBackButtonClick = {
                navman.goBack()
            },
            displayAppBar = displayAppBar
        )
    }
}

@Composable
fun PetDetailsUI(
    pet: PetListItemInfo?,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    onAdoptClick: () -> Unit,
    onBackButtonClick: () -> Unit,
    displayAppBar: Boolean = true
) {

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.appColorTheme.materialColors.surface)
    ) {

        if (displayAppBar) {
            TopAppBar(
                modifier = Modifier.height(ScreenGlobals.DefaultToolbarHeight),
                elevation = 0.dp,
                title = {
                    if (pet != null) {
                        Text(pet.name, color = AppTheme.appColorTheme.materialColors.secondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackButtonClick) {
                        Icon(
                            modifier = modifier
                                .requiredWidth(ScreenGlobals.ToolbarBackButtonIconSize)
                                .requiredHeight(ScreenGlobals.ToolbarBackButtonIconSize),
                            tint = AppColors.turquoise,
                            imageVector = Icons.Filled.ArrowLeft,
                            contentDescription = ""
                        )
                    }
                }
            )
        } else {
            Spacer(modifier = Modifier.requiredHeight(47.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                if (pet != null) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 10.dp)
                            .fillMaxSize()
                    ) {
                        PetImageGalleryHandler(pet = pet)
                    }
                    Column(
                        verticalArrangement = Arrangement.Top, modifier = modifier
                            .weight(1f)
                            .padding(start = 10.dp, top = 0.dp, end = 10.dp, bottom = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(width = 2.dp, color = MaterialColors.gray100, shape = RoundedCornerShape(10.dp))
                            .background(AppColors.whiteAlpha)
                    ) {
                        Column(modifier = Modifier.padding(top = 20.dp)) {
                            PetDetailStatsUI(
                                pet = pet,
                                modifier = Modifier.fillMaxWidth(),
                                onAdoptClick = onAdoptClick
                            )
                        }
                    }
                }
            }
        }
    }
}