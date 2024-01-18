package io.github.johannrosenberg.sample.ui.screens.petdetails

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.annotation.ExperimentalCoilApi
import coil.compose.LocalImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import io.github.johannrosenberg.jetmagic.composables.IImageManager
import io.github.johannrosenberg.jetmagic.composables.ManagedImageHandler
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.models.PetListItemInfo
import io.github.johannrosenberg.sample.ui.theme.MaterialColors
import io.github.johannrosenberg.sample.utils.DeviceUtils

@OptIn(ExperimentalCoilApi::class)
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
            imageManager.updateImagePath(id = "large", imagePath = pathToLargeImage)
            //imageManager.updateState(id = "large", imagePath = pathToLargeImage, animate = true)
        })
}

@OptIn(ExperimentalCoilApi::class)
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

        if (pet.imageCount == 0) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(largeImageSize)
                    .padding(15.dp)
                    .border(border = BorderStroke(width = 1.dp, MaterialColors.gray900))
                    , verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center
            ) {
                Text("No images available in this demo for this cat. Click on the first cat on the list to view thumbnail images...", modifier = Modifier.padding(20.dp))
            }
        } else {
            ManagedImageHandler(
                id = "large",
                imagePath = largeImagePath,
                modifier = Modifier
                    .fillMaxWidth()
                    .requiredHeight(largeImageSize)
            ) { imageUrl, animate ->
                rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(data = imageUrl).apply(block = fun ImageRequest.Builder.() {
                    crossfade(animate)
                }).build())
            }
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
                for (i in 1..pet.imageCount) {
                    Column(
                        modifier = modifier
                            .requiredWidth(94.dp)
                            .requiredHeight(100.dp)
                    ) {

                        ManagedImageHandler(
                            id = "thumbnail-" + pet.id + "-" + i,
                            imagePath = "pet_" + pet.id + "_" + i,
                            modifier = Modifier
                                .requiredWidth(90.dp)
                                .requiredHeight(90.dp)
                                .clickable {
                                    onThumbnailClick(pet.id.toString(), i)
                                },
                        ) { imageUrl, animate ->
                            rememberAsyncImagePainter(ImageRequest.Builder(LocalContext.current).data(data = imageUrl).apply(block = fun ImageRequest.Builder.() {
                                crossfade(true)
                            }).build(), imageLoader = LocalImageLoader.current)
                        }
                    }
                }
            }
        }
    }
}

fun getGalleryLargeImagePath(petId: String, imageIndex: Int): String {
    return "pet_" + petId + "_" + imageIndex
}