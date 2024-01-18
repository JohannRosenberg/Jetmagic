package io.github.johannrosenberg.sample.ui.screens.petslist

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.annotation.ExperimentalCoilApi
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.github.johannrosenberg.jetmagic.composables.ManagedImageHandler
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.ComposableParams
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.models.PetListItemInfo
import io.github.johannrosenberg.sample.ui.components.ImageGrid
import io.github.johannrosenberg.sample.ui.components.ListLoadingIndicator
import io.github.johannrosenberg.sample.ui.screens.ScreenGlobals
import io.github.johannrosenberg.sample.ui.screens.main.MainViewModel
import io.github.johannrosenberg.sample.utils.DeviceUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PetsListHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {

        val coroutineScope = rememberCoroutineScope()

        val vmMain: MainViewModel = viewModel()
        val vm = composableInstance.viewmodel as PetsListViewModel
        vm.imageManager.onComposableInstanceTerminated(composableInstance = composableInstance)
        val petsList = vm.onPetsAvailable.observeAsState().value

        val p = composableInstance.parameters as ComposableParams?
        val modifier = p?.modifier ?: Modifier

        if (!composableInstance.isTerminated && !petsList.isNullOrEmpty()) {
            vm.updatePetDetailsIfPresent(composableInstance = composableInstance, petInfo = petsList[0])
        }

        PetsList(
            modifier = modifier,
            petsList = petsList,
            scrollState = vm.scrollState,
            onItemClick = { petInfo ->
                vm.updateOrGotoPetDetails(composableInstance = composableInstance, petInfo = petInfo)
            },
            onToolbarMenuClick = {
                coroutineScope.launch {
                    vmMain.drawerState.open()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetsList(
    modifier: Modifier = Modifier,
    petsList: List<PetListItemInfo>? = null,
    scrollState: ScrollState,
    onItemClick: (petInfo: PetListItemInfo) -> Unit,
    onToolbarMenuClick: () -> Unit
) {
    val gridPetNameFontSize = if (DeviceUtils.isATablet()) 24.sp else 14.sp

    if (petsList != null) {
        val toolbarHeight = ScreenGlobals.DefaultToolbarHeight
        val toolbarHeightPx = with(LocalDensity.current) { toolbarHeight.roundToPx().toFloat() }

        // Offset to collapse toolbar
        val toolbarOffsetHeightPx = remember { mutableStateOf(0f) }

        // Create a connection to the nested scroll system and listen to the scroll happening inside child Column
        val nestedScrollConnection = remember {
            object : NestedScrollConnection {
                override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                    val delta = available.y
                    val newOffset = toolbarOffsetHeightPx.value + delta
                    toolbarOffsetHeightPx.value = newOffset.coerceIn(-toolbarHeightPx, 0f)
                    return Offset.Zero
                }
            }
        }

        Box(
            modifier = modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
        ) {
            Surface(modifier = modifier.fillMaxSize()) {
                Row(modifier = modifier.fillMaxSize()) {
                    Box(
                        modifier = modifier
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)

                        ) {
                            Row(
                                modifier = modifier
                                    .fillMaxWidth()
                                    .requiredHeight(toolbarHeight)
                            ) {
                            }

                            ImageGrid(
                                modifier = modifier,
                                items = petsList
                            ) { petInfo ->
                                PetGridItem(
                                    pet = petInfo,
                                    gridPetNameFontSize = gridPetNameFontSize,
                                    onItemClick = onItemClick
                                )
                            }
                        }
                    }
                }
            }

            TopAppBar(
                modifier = Modifier
                    .height(toolbarHeight)
                    .offset { IntOffset(x = 0, y = toolbarOffsetHeightPx.value.roundToInt()) },
                title = {},
                navigationIcon = {
                    IconButton(onClick = onToolbarMenuClick) {
                        Icon(
                            tint = MaterialTheme.colorScheme.primary,
                            imageVector = Icons.Filled.Menu,
                            contentDescription = ""
                        )
                    }
                }
            )
        }
    } else {
        ListLoadingIndicator()
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
fun PetGridItem(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier,
    gridPetNameFontSize: TextUnit,
    onItemClick: (PetListItemInfo) -> Unit
) {
    ManagedImageHandler(
        id = "grid-image-" + pet.id,
        imagePath = "pet_" + pet.id + "_s", // "$PetsThumbnailImagesPath${pet.id}-1.jpg",
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onItemClick(pet)
            }
    ) { imageUrl, animate ->
        rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(data = imageUrl).apply(block = fun ImageRequest.Builder.() {
            crossfade(animate)
        }).build())
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
        Text(
            pet.name, modifier = modifier
                .fillMaxWidth()
                .padding(start = 5.dp), color = Color.White,
            fontSize = gridPetNameFontSize
        )
    }
}