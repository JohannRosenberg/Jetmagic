package dev.wirespec.jetmagic.models

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import dev.wirespec.jetmagic.composables.*

/**
 * Represents a composable resource.
 *
 * A composable resource acts as a template similar to how an xml layout
 * works under the older view system. The [ComposableResourceManager] uses the composable resource when it
 * needs to create a [composable instance][ComposableInstance]. Similar to how configuration qualifiers
 * are used with xml layouts, composable resources can also define qualifiers through properties. The
 * Composable Resource Manager will determine which composable resource to use based on the current
 * device configuration. Requests to the CRM can be made to create composable instances from composable
 * resources. A composable instance is rendered on the screen.For details on each qualifier, see:
 *
 * [https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources]
 *
 * A composable resource that has not set any of its configuration qualifiers is
 * referred to as the default resource. Multiple composable resources can be defined that use the same
 * [resourceId] property but one - and only one - must exist that acts as the default. An exception is
 * thrown by [addComposableResources][ComposableResourceManager.addComposableResources] if no default is
 * defined for each unique resourceId.
 *
 * A composable resource can also optionally define a viewmodel. When a composable instance is created,
 * an instance of the viewmodel is created and assigned to the composable instance.
 *
 */
open class ComposableResource(

    /**
     * The id provided by the app to identify the resource. This is not unique. The same id can be used
     * for a different set of qualifier configurations.
     */
    val resourceId: String,

    /**
     * The class that will be used to create an instance of the viewmodel associated with an instance of the composable.
     * The viewmodel will only be created if onCreateViewmodel is null. viewmodelClass is a general purpose way of
     * creating a viewmodel that doesn't have any constructor parameters and there is no need for a more elaborate way
     * to create a viewmodel. If you need to have control over how the viewmodel is created, use onCreateViewmodel.
     * viewmodelClass only takes a reference to the class. For example: PetsListViewModel::class.java
     */
    val viewmodelClass: Class<*>? = null,

    /**
     * A callback that can be used to create the viewmodel. If provided, the viewmodelClass property must also be set to
     * indicate the viewmodel class that you are implementing. The viewmodelClass will only be used during device
     * configuration changes to determine if an updated resource needs to use a different viewmodel than the one it
     * may have previously had. If onCreateViewmodel is specified but viewmodelClass is left null, an exception will
     * be thrown when [addComposableResources] is called.
     */
    val onCreateViewmodel: (() -> ViewModel)? = null,

    /**
     * The mobile country code (MCC). If the MNC qualifier is also provided, it will be combined with the MCC qualifier
     * to act as a single qualifier.
     */
    val mcc: Int? = null,

    /**
     * The mobile network code (MNC). If the MCC qualifier is not specified, the MNC qualifier will be ignored.
     */
    val mnc: Int? = null,

    /**
     * The language and region. Example "en-ca" for english, Canada.
     * Note: BCP 47 language tags are currently not supported.
     */
    val languageAndRegion: String? = null,

    /**
     * Layout direction is either left-to-right or right-to-left.
     */
    var layoutDirection: LayoutDirection? = null,

    /**
     * Smallest screen width specified in dp.
     */
    var smallestWidthInDp: Int? = null,

    /**
     * The available screen width in dp.
     */
    val availableWidthInDp: Int? = null,

    /**
     * The available screen height in dp.
     */
    var availableHeightInDp: Int? = null,

    /**
     * The size of the screen using generic approximate sizes.
     */
    var screenSize: ScreenSize? = null,

    /**
     * The screen's aspect ratio in a generic format.
     */
    var screenAspect: ScreenAspect? = null,

    /**
     * The screen's shape.
     */
    var roundScreen: RoundScreen? = null,

    /**
     * The screen's color gamut.
     */
    var wideColorGamut: WideColorGamut? = null,

    /**
     * Displays with high or low dynamic ranges.
     */
    var highDynamicRange: HighDynamicRange? = null,

    /**
     * The screen orientation: portrait or landscape.
     */
    var screenOrientation: ScreenOrientation? = null,

    /**
     * Type of device the screen is being displayed on.
     */
    val uiMode: UIMode? = null,

    /**
     * Night mode.
     */
    var nightMode: NightMode? = null,

    /**
     * The screen's pixel density in dpi. Use constants defined in ScreenPixelDensityDpi or
     * set this property to a dpi value (which is the equivalent of setting the nnndpi qualifier
     * value).
     */
    var screenPixelDensityDpi: Int? = null,

    /**
     * Whether touch is used on the screen.
     */
    var touchScreenType: TouchScreenType? = null,

    /**
     * Type of keyboard available.
     */
    var keyboardAvailability: KeyboardAvailability? = null,

    /**
     * Primary means of entering text.
     */
    var primaryTextInputMethod: PrimaryTextInputMethod? = null,

    /**
     * Whether navigation keys are available.
     */
    var navigationKeyAvailability: NavigationKeyAvailibility? = null,

    /**
     * The primary method used to interact that is non-touch.
     */
    var primaryNonTouchNavigationMethod: PrimaryNonTouchNavigationMethod? = null,

    /**
     * The API level supported by the device. Example, 1, 2, 3...30, etc.
     */
    var platformVersion: Int? = null,

    /**
     * A callback that will be called to allow the composable to provide animation when made visible or hidden.
     * The invisible parameter will be set to true to indicate that composable is being made visible and set
     * to false when hidden.
     */
    var onAnimateVisibility: (@Composable (composableInstance: ComposableInstance) -> Unit)? = null,

    /**
     * A callback that will be called to render the composable.
     */
    var onRender: @Composable (composableInstance: ComposableInstance) -> Unit,


    ) {
    /**
     * Each composable resource added with [addComposableResources][ComposableResourceManager.addComposableResources] will be assigned a unique id and provided in this property.
     * The client has no access to this id. It helps [ComposableResourceManager] to identify each composable resource added
     * with addComposableResources.
     */
    internal var id: String? = null
}
