package io.github.johannrosenberg.jetmagic.composables

import android.annotation.SuppressLint
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
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.jetmagic.utils.ScreenUtils

/**
 * Handles the creation and update of an image.
 *
 * @param id A unique id to identify the image on the screen.
 *
 * @param imagePath Indicates the source of the image. If the imagePath starts with either "https:" or "http",
 * the image is considered to be downloaded from the web, otherwise the path indicates the name of a resource
 * located under the res folder - for example, an image resource called "my_image.jpg" would set imagePath to
 * "my_image" as a string. Don't pass R.drawable.my_image.
 *
 * @param modifier Any custom modifications will be applied to the image.
 *
 * @param painterFactory A callback that will be called whenever a painter is needed
 * to displayed the image. The [ImagePainter] that the callback returns can be
 * customized to provide a customized painter.
 */
@SuppressLint("DiscouragedApi")
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
    painterFactory: @Composable (imagePath: Any, animate: Boolean) -> ImagePainter,
) {
    val imageMan = (LocalComposableInstance.current.viewmodel as IImageManager).imageManager
    imageMan.onImageUpdated.value

    val painter = imageMan.getPainter(id = id, imagePath = imagePath) { imageUrl, animate ->
        val src =
            if (imageUrl.startsWith("https:") || imageUrl.startsWith("http:"))
                imageUrl
            else
                ScreenUtils.context.resources.getIdentifier(imageUrl, "drawable", ScreenUtils.context.packageName)

        painterFactory(src, animate)
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