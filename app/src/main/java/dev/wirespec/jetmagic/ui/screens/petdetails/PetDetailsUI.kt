package dev.wirespec.jetmagic.ui.screens.petdetails

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.models.PetDetailsParams
import dev.wirespec.jetmagic.models.PetListItemInfo
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs
import dev.wirespec.jetmagic.ui.screens.ScreenGlobals
import dev.wirespec.jetmagic.ui.theme.AppColors
import dev.wirespec.jetmagic.ui.theme.AppTheme
import dev.wirespec.jetmagic.ui.theme.MaterialColors

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    // Get notified of updates.
    composableInstance.onUpdate?.observeAsState()?.value

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        val vm = composableInstance.viewmodel as PetDetailsViewModel
        vm.imageManager.onComposableInstanceTerminated(composableInstance = composableInstance)
        vm.processDeepLink(composableInstance = composableInstance)

        val p = composableInstance.parameters as PetDetailsParams?
        val modifier: Modifier = p?.modifier ?: Modifier
        var pet = p?.petsListItemInfo

        if ((pet == null) && (vm.petInfo != null)) {
            pet = vm.petInfo
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
            })
    }
}

@ExperimentalCoilApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PetDetailsUI(
    pet: PetListItemInfo?,
    scrollState: ScrollState,
    modifier: Modifier = Modifier,
    onAdoptClick: () -> Unit,
    onBackButtonClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppTheme.appColorTheme.materialColors.surface)
    ) {

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
        Column(
            verticalArrangement = Arrangement.Top, modifier = modifier
                .fillMaxSize()
                // Important: verticalScroll must be before padding to prevent click events along the padded edge
                // from propagating to the screen below.
                .verticalScroll(scrollState)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .border(width = 2.dp, color = MaterialColors.gray100, shape = RoundedCornerShape(10.dp))
                .background(AppColors.whiteAlpha)

        ) {
            if (pet != null) {
                PetImageGalleryHandler(pet)
                Spacer(modifier.requiredHeight(20.dp))
                PetDetailStatsUI(
                    pet = pet,
                    modifier = Modifier.fillMaxWidth(),
                    onAdoptClick = onAdoptClick
                )
            }
        }
    }
}