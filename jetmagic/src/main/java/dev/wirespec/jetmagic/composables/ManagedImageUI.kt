package dev.wirespec.jetmagic.composables

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import dev.wirespec.jetmagic.models.LocalComposableInstance

/**
 * Handles the creation and update of an image.
 *
 * @param id A unique id to identify the image on the screen.
 *
 * @param imagePath The url to the image that will be displayed.
 *
 * @param modifier Any custom modifications will be applied to the image.
 *
 * @param painterFactory A callback that will be called whenever a painter is needed
 * to displayed the image. The [ImagePainter] that the callback returns can be
 * customized to provide a customized painter.
 */
@ExperimentalCoilApi
@Composable
fun ManagedImageHandler(
    id: String,
    imagePath: String,
    modifier: Modifier = Modifier,
    painterFactory: @Composable (imagePath: String, animate: Boolean) -> ImagePainter
) {
    val imageMan = (LocalComposableInstance.current.viewmodel as IImageManager).imageManager

    val painter = imageMan.getPainter(id = id, imagePath = imagePath) { imageUrl, animate ->
        painterFactory(imagePath = imageUrl, animate = animate)
    }

    // Get notified whenever the image is updated.
    // IMPORTANT: This must be called AFTER animState.getPainter is called because getPainter sets up
    // the state if one doesn't exist.
    imageMan.getImageState(id).onUpdate.observeAsState().value

    Image(
        painter = painter,
        modifier =  modifier
    )
}

/**
 * This is essentially a standard Compose Image composable with a customized painter.
 *
 * @param painter The custom painter that will be applied to the image.
 *
 * @param modifier Any custom modifications will be applied to the image.
 */
@Composable
fun Image(
    painter: Painter,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painter,
        contentDescription = null,
        modifier = modifier,
        contentScale = ContentScale.Crop
    )
}