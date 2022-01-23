package dev.wirespec.jetmagic.composables

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
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
    alignment: Alignment = Alignment.TopCenter,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    painterFactory: @Composable (imagePath: String, animate: Boolean) -> ImagePainter,
) {
    val imageMan = (LocalComposableInstance.current.viewmodel as IImageManager).imageManager
    imageMan.onImageUpdated.value

    val painter = imageMan.getPainter(id = id, imagePath = imagePath) { imageUrl, animate ->
        painterFactory(imagePath = imageUrl, animate = animate)
    }

    // Get notified whenever the image is updated.
    // IMPORTANT: This must be called AFTER animState.getPainter is called because getPainter sets up
    // the state if one doesn't exist.
    imageMan.getImageState(id).onUpdate.observeAsState().value

    Image(
        modifier = modifier,
        painter = painter,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
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
    modifier: Modifier = Modifier,
    painter: Painter,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Crop,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null
) {
    Image(
        modifier = modifier,
        painter = painter,
        contentDescription = null,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter
    )
}