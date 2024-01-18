package io.github.johannrosenberg.sample.ui.screens.petdetails

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.models.PetDetailsParams
import io.github.johannrosenberg.sample.models.PetListItemInfo
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs
import io.github.johannrosenberg.sample.ui.screens.ScreenGlobals
import io.github.johannrosenberg.sample.ui.theme.AppColors
import io.github.johannrosenberg.sample.ui.theme.MaterialColors

@OptIn(ExperimentalCoilApi::class)
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

@OptIn(ExperimentalMaterial3Api::class)
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
            .background(MaterialTheme.colorScheme.surface)
    ) {

        TopAppBar(
            modifier = Modifier.height(ScreenGlobals.DefaultToolbarHeight),
            title = {
                if (pet != null) {
                    Row(modifier = Modifier.fillMaxHeight(), verticalAlignment = Alignment.CenterVertically) {
                        Text(pet.name, color = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBackButtonClick) {
                    Icon(
                        modifier = modifier
                            .requiredWidth(ScreenGlobals.ToolbarBackButtonIconSize)
                            .requiredHeight(ScreenGlobals.ToolbarBackButtonIconSize),
                        tint = MaterialTheme.colorScheme.primary,
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
                .padding(start = 10.dp, end = 10.dp, bottom = 10.dp)
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