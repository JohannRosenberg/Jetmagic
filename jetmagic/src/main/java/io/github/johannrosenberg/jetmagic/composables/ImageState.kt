package io.github.johannrosenberg.jetmagic.composables

import androidx.compose.foundation.Image
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import coil.compose.ImagePainter

/**
 * Maintains the state of an image.
 *
 * @param imagePath The url to the image being displayed.
 *
 * @param animate If set to true, the image will use animations/transitions.
 *
 * @param imagePainter The painter object to cache that will be used to draw the [Image].
 */
data class ImageState(var imagePath: String, var animate: Boolean = false, var imagePainter: ImagePainter? = null) {
    private val _onUpdate = MutableLiveData(0)
    val onUpdate: LiveData<Int> = _onUpdate

    /**
     * Updates the state of the image.
     *
     * @param imagePath The updated url for the image being displayed.
     *
     * @param animate If set to true, the image will use animations/transitions.
     */
    fun updateState(imagePath: String, animate: Boolean) {
        this.imagePath = imagePath
        this.animate = animate
        _onUpdate.value = (0..1_000_000).random()
    }
}

