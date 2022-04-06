package dev.wirespec.sample.ui.screens.petdetails

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.LocalImageLoader
import coil.compose.rememberImagePainter
import dev.wirespec.jetmagic.composables.IImageManager
import dev.wirespec.jetmagic.composables.ManagedImageHandler
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.sample.da.web.PetsLargeImagesPath
import dev.wirespec.sample.da.web.PetsThumbnailImagesPath
import dev.wirespec.sample.models.PetListItemInfo
import dev.wirespec.sample.utils.DeviceUtils

@Composable
fun PetImageGalleryHandler(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier
) {
    val vm = LocalComposableInstance.current.viewmodel as PetDetailsViewModel
    val imageManager = (vm as IImageManager).imageManager

    PetImageGallery(
        pet = pet,
        modifier = modifier,
        thumbnailScrollState = ScrollState(0),
        onThumbnailClick = { petId, selectedThumbnailNumber ->
            val pathToLargeImage = getGalleryLargeImagePath(petId = petId, imageIndex = selectedThumbnailNumber)
            imageManager.updateState(id = "large", imagePath = pathToLargeImage, animate = true)
        })
}

@Composable
fun PetImageGallery(
    pet: PetListItemInfo,
    modifier: Modifier = Modifier,
    thumbnailScrollState: ScrollState,
    onThumbnailClick: (petId: String, selectedThumbnailNumber: Int) -> Unit
) {
    val largeImageSize = if (DeviceUtils.isATablet()) 730.dp else 400.dp

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        val largeImagePath = getGalleryLargeImagePath(petId = pet.id.toString(), imageIndex = 1)

        ManagedImageHandler(
            id = "large",
            imagePath = largeImagePath,
            modifier = Modifier
                .fillMaxWidth()
                .requiredHeight(largeImageSize)
        ) { imageUrl, animate ->
            rememberImagePainter(
                data = imageUrl,
                builder = {
                    crossfade(animate)
                }
            )
        }

        if (pet.imageCount > 1) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .requiredHeight(100.dp)
                    .padding(top = 4.dp)
                    .horizontalScroll(thumbnailScrollState),
                horizontalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                for (i in 1 until pet.imageCount + 1) {
                    Column(
                        modifier = modifier
                            .requiredWidth(94.dp)
                            .requiredHeight(100.dp)
                    ) {

                        ManagedImageHandler(
                            id = "thumbnail-" + pet.id + "-" + i,
                            imagePath = PetsThumbnailImagesPath + pet.id + "-" + i + ".jpg",
                            modifier = Modifier
                                .requiredWidth(90.dp)
                                .requiredHeight(90.dp)
                                .clickable {
                                    onThumbnailClick(pet.id.toString(), i)
                                },
                        ) { imageUrl, animate ->
                            rememberImagePainter(
                                data = imageUrl,
                                imageLoader = LocalImageLoader.current,
                                builder = {
                                    crossfade(true)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

fun getGalleryLargeImagePath(petId: String, imageIndex: Int): String {
    return "$PetsLargeImagesPath$petId-$imageIndex.jpg"
}