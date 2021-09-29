package dev.wirespec.jetmagic.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter
import dev.wirespec.jetmagic.models.ComposableInstance


/**
 * Provides support to manage composable images. This class should be instantiated in a viewmodel that is
 * attached to the root composable instance of a screen. One of the key functions of ImageManager is to
 * cache and update the painter object for Image composables and provide either a newly constructed painter
 * when required or to reuse the cached painter such as when the user is exiting a screen (like hitting
 * the Back button). Reusing a cached painter is needed when an image is displayed on a screen that uses
 * animated visibility. Without restoring the cached painter prior to returning to the previous screen,
 * the image will flicker. This is due to an image being recomposed during the screen's animated exit and
 * the [rememberImagePainter] gets destroyed. Caching the state of a painter restores the state of an Image
 * without Compose having to recompose the image.
 */
@ExperimentalCoilApi
class ImageManager {
    private val states = mutableMapOf<String, ImageState>()

    /**
     * Performs any cleanup work when the composable instance that images are located on is terminated.
     * This should be called when a root composable instance is rendered and prior to any images that are
     * managed by the ImageManager are rendered.
     */
    fun onComposableInstanceTerminated(composableInstance: ComposableInstance) {
        if (composableInstance.isTerminated) {
            states.forEach {
                it.value.animate = false
            }
        }
    }

    /**
     * Clears the state for all images.
     */
    fun clearAllImageStates() {
        states.clear()
    }

    /**
     * Returns the painter that is to be used for drawing an image.
     *
     * @param id The id of the image that the painter is associated with. Each image has its own painter whose state
     * will get cached and reused whenever animations for an image is disabled. This would be the case when the
     * screen on which the image is located uses a visibility animation and is being terminated (removed from
     * the navigation stack).
     *
     * @param imagePath The url to the image that will be displayed.
     *
     * @param painterFactory A callback that will be called to obtain the painter to be used by the image. The callback
     * will not be called when the composable instance that the current screen is bound to is terminated - which happens when
     * the user hits the back button or returns to the home screen.
     *
     * @return The painter returned can be customized.
     */
    @Composable
    fun getPainter(id: String, imagePath: String, painterFactory: @Composable (imagePath: String, animate: Boolean) -> ImagePainter): Painter {

        var state = states[id]

        if (state == null) {
            state = ImageState(imagePath = imagePath)
            states[id] = state
        }

        return if (!state.animate && (state.imagePainter != null) && (state.imagePainter!!.state.painter != null)) {
            // This is the key to preventing an image from flickering when the image contains a
            // transition (such as crossfade) and the image is located on a screen that
            // is being animated to become invisible (which is what happens when the user
            // hits the back button on a screen with a visibility animation). Reusing the last
            // painter prevents the flicker.
            state.imagePainter!!.state.painter!!
        } else {
            val imagePainter = painterFactory(state.imagePath, state.animate)

            if (imagePainter.state.painter != null) {
                state.imagePainter = imagePainter
            }

            imagePainter
        }
    }

    /**
     * Returns the image state for the specified image.
     *
     * @param id The same id as when calling [getPainter].
     */
    fun getImageState(id: String): ImageState {
        return states[id] as ImageState
    }

    /**
     * Updates the state for an image.
     *
     * @param id The same id as when calling [getPainter].
     *
     * @param imagePath The updated path (url) to image.
     *
     * @param animate Set to true if the image requires a painter that will include support
     * for animations/transitions such as crossfade. Set to false to disable animations/tranitions.
     */
    fun updateState(id: String, imagePath: String, animate: Boolean = false) {
        val state = states[id]
        state?.updateState(imagePath = imagePath, animate = animate)
    }
}

/**
 * An interface that viewmodels need to implement in order to access the functions of [ImageManager]
 */
@ExperimentalCoilApi
interface IImageManager {
    /**
     * Returns a reference to the instance of an ImageManager.
     */
    val imageManager: ImageManager
}

