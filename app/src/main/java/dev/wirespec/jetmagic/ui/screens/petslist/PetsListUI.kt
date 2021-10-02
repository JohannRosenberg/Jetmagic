package dev.wirespec.jetmagic.ui.screens.petslist

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberImagePainter
import dev.wirespec.jetmagic.composables.ManagedImageHandler
import dev.wirespec.jetmagic.da.web.PetsThumbnailImagesPath
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableParams
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.models.PetListItemInfo
import dev.wirespec.jetmagic.ui.components.ImageGrid
import dev.wirespec.jetmagic.ui.components.ListLoadingIndicator
import dev.wirespec.jetmagic.ui.screens.ScreenGlobals
import dev.wirespec.jetmagic.ui.screens.main.MainViewModel
import dev.wirespec.jetmagic.ui.theme.AppColors
import dev.wirespec.jetmagic.utils.DeviceUtils
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

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
                    vmMain.scaffoldState.drawerState.open()
                }
            }
        )
    }
}

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
                elevation = 0.dp,
                title = {},
                navigationIcon = {
                    IconButton(onClick = onToolbarMenuClick) {
                        Icon(
                            tint = AppColors.turquoise,
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

@Composable
fun PetGridItem(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier,
    gridPetNameFontSize: TextUnit,
    onItemClick: (PetListItemInfo) -> Unit
) {
    ManagedImageHandler(
        id = "grid-image-" + pet.id,
        imagePath = "$PetsThumbnailImagesPath${pet.id}-1.jpg",
        modifier = Modifier
            .fillMaxSize()
            .clickable {
                onItemClick(pet)
            }
    ) { imageUrl, animate ->
        rememberImagePainter(
            data = imageUrl,
            builder = {
                crossfade(animate)
            }
        )
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