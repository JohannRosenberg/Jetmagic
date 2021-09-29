# Jetmagic - ImageManager

Provides support to manage composable images. This class should be instantiated in a viewmodel that is attached to the root composable instance of a screen. One of the key functions of ImageManager is to cache and update the painter object for Image composables and provide either a newly constructed painter when required or to reuse the cached painter such as when the user is exiting a screen (like hitting the Back button). Reusing a cached painter is needed when an image is displayed on a screen that uses animated visibility. Without restoring the cached painter prior to returning to the previous screen, the image will flicker. This is due to an image being recomposed during the screen's animated exit and the rememberImagePainter gets destroyed. Caching the state of a painter restores the state of an Image without Compose having to recompose the image.

#### Table of Contents

* How to:
  - [Setting up Image Manager](#setting_up_image_manager)

* APIs
  - [ImageManager (Class)](#imagemanager-class)
  - [ImageState (Data Class)](#imagestate-data-class)
  - [ManagedImageHandler (Composable)](#managedimagehandler-composable)
  - [IImageManager (Interface)](#iimagemanager-interface)


<a name="setting_up_image_manager" />

<br />

### Setting up Image Manager

In order to use the Image Manager, you **must** have a viewmodel that is managed by Jetmagic. This is because the **ManagedImageHandler** needs to access your viewmodel where it expects to find an instance of the Image Manager. In the demo app, the PetDetailsViewModel implements the **IImageManager** interface:

```kotlin
class PetDetailsViewModel: ViewModel(), IImageManager {

    private val imageMan = ImageManager()

    override val imageManager: ImageManager
        get() = imageMan

}
```

Your composable instance **must** use **CompositionLocalProvider** together with **LocalComposableInstance**. In the demo app, the PetDetailsHandler does it like this:

```kotlin
@Composable
fun PetDetailsHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        val vm = composableInstance.viewmodel as PetDetailsViewModel
        vm.imageManager.onComposableInstanceTerminated(composableInstance = composableInstance)

        // ...
    }
}
```

Here is an example of how the demo app uses a Jetmagic **ManagedImageHandler** in **PetImageGallery**:

```kotlin
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
    }
}
```

The last parameter in ManagedImageHandler is **painterFactory** which is a lambda callback. When the composable is rendered, the callback will be called and provided with the url to the image and a boolean parameter called **animate**, which if set true means that the image can provide animation/transition effects. The callback must return an ImagePainter. If the image painter uses animations/transitions when animate is set to false, the image will flicker when the screen exits.

Every ManagedImageHandler must have a unique id on the screen. How you create your ImagePainter is your choice. The example used in the demo is just one. There are many ways to customize an ImagePainter. For details on how to do this see:

[https://coil-kt.github.io/coil/compose/](https://coil-kt.github.io/coil/compose/)

It should be noted that although the ManagedImageHandler uses **rememberImagePainter** which manages the state of the image, this in fact will get destroyed when the composable is recomposed. Internally, the ImageManager maintains the state of the painter that is needed when recomposing occurs and will provide a cached state when the screen is exited, to ensure that no image flickering occurs.

If you have a screen displaying images rendered with **ManagedImageHandler** and the screen will terminate because the user either returns to the previous screen or to the home screen, you need to call **onComposableInstanceTerminated**. This will ensure that animations/transitions for the images are disabled to avoid flickering. In the sample code above, this is called without the need to check if the composable instance's **isTerminated** property is set to true. Just pass the composable instance to  **onComposableInstanceTerminated** and the check for **isTerminated** will be made. You can pass either the parent composable instance or one of it's children.

If you want to see how flickering looks when animations/transitions are not disabled, either don't call **onComposableInstanceTerminated** or in the source code for **onComposableInstanceTerminated**, comment out the line:

```kotlin
it.value.animate = false
```

<a name="image_manager" /><br />

## ImageManager (Class)

| Function / Property            | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| ------------------------------ | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| clearAllImageStates            | ```fun clearAllImageStates()```<br /><br />Clears the state for all images.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         |
| getImageState                  | ```fun getImageState(id: String): ImageState```<br /><br />Returns the image state for the specified image.<br/><br/>**id:** The same id as when calling getPainter.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |
| getPainter                     | ```fun getPainter(id: String, imagePath: String, painterFactory: @Composable (imagePath: String, animate: Boolean) -> ImagePainter): Painter```<br /><br />Returns the painter that is to be used for drawing an image.<br/><br/>**id:** The id of the image that the painter is associated with. Each image has its own painter whose state will get cached and reused whenever animations for an image is disabled. This would be the case when the screen on which the image is located uses a visibility animation and is being terminated (removed from the navigation stack).<br/><br/>**imagePath:** The url to the image that will be displayed.<br/><br/>**painterFactory:** A callback that will be called to obtain the painter to be used by the image. The callback will not be called when the composable instance that the current screen is bound to is terminated - which happens when the user hits the back button or returns to the home screen.<br/><br/> **returns:** The painter returned can be customized. |
| onComposableInstanceTerminated | ```fun onComposableInstanceTerminated(composableInstance: ComposableInstance)```<br /><br />Performs any cleanup work when the composable instance that images are located on is terminated. This should be called when a root composable instance is rendered and prior to any images that are managed by the ImageManager are rendered.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           |
| updateState                    | ```fun updateState(id: String, imagePath: String, animate: Boolean = false)```<br /><br />Updates the state for an image.<br/><br/>**id:** The same id as when calling getPainter.<br/><br/>**imagePath:** The updated path (url) to image.<br/><br/>**animate:** Set to true if the image requires a painter that will include support for animations/transitions such as crossfade. Set to false to disable animations/tranitions.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                |

<a name="image_state" /><br />

## ImageState (Data Class)

Maintains the state of an image.

| constructor                                                                                                                                     | Description                                                                                                                                                                                                                  |
| ----------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| data class ImageState(<br/>        var imagePath: String, <br/>        var animate: Boolean = false, <br/>        var painter: Painter? = null) | **imagePath:** The url to the image being displayed.<br/><br/>**animate:** If set to true, the image will use animations/transitions.<br/><br/>**painter:** The painter object to cache that will be used to draw the image. |

| Function / Property | Description                                                                                                                                                                                                                                                   |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| updateState         | ```fun updateState(imagePath: String, animate: Boolean)```<br /><br />Updates the state of the image.<br/><br/>**imagePath:** The updated url for the image being displayed.<br/><br/>**animate:** If set to true, the image will use animations/transitions. |

<a name="managed_image_handler" /><br />

## ManagedImageHandler (Composable)

An image composable that is managed by the Image Manager.

| Function / Property | Description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               |
| ------------------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| ManagedImageHandler | ```fun ManagedImageHandler(id: String, imagePath: String, modifier: Modifier = Modifier,painterFactory: @Composable (imagePath: String, animate: Boolean) -> ImagePainter)```<br /><br />Handles the creation and update of an image.<br/><br/>**id:** A unique id to identify the image on the screen.<br/><br/>**imagePath:** The url to the image that will be displayed.<br/><br/>**modifier:** Any custom modifications will be applied to the image.<br/><br/>**painterFactory:** A callback that will be called whenever a painter is needed to displayed the image. The ImagePainter that the callback returns can be customized to provide a customized painter. |

<a name="i_image_manager" /><br />

## IImageManager (Interface)

An interface that viewmodels need to implement in order to access the functions of ImageManager

| Function / Property | Description                                                                                             |
| ------------------- | ------------------------------------------------------------------------------------------------------- |
| imageManager        | ```val imageManager: ImageManager```<br /><br />Returns a reference to the instance of an ImageManager. |