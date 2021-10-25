package dev.wirespec.jetmagic.composables

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.content.res.Configuration.*
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableResource
import dev.wirespec.jetmagic.models.DeepLink
import dev.wirespec.jetmagic.navigation.NavigationManager
import dev.wirespec.jetmagic.utils.ScreenUtils
import java.util.*


/**
 * The Composable Resource Manager (abbreviated as CRM) is used to manage Jetpack composables to allow for the support of
 * responsive screen layouts that are dependent on device configurations. It acts in a similar way to how Android manages
 * the selection of layouts when using the older xml based view system.
 *
 * The CRM supports all of the configuration qualifiers that are supported by the xml based system as listed at:
 * [https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources]
 * A qualifier property will only be used if it is not set to null.
 *
 * Instead of text qualifiers that are applied to directories, the CRM provides properties that can be set for each
 * qualifier. Unlike qualifiers with directories where the qualifiers must be specified in order of precedence, there
 * is no need to specify any order. The CRM handles the order of qualifiers internally.
 *
 * Instead of xml layout files as resources, the CRM treats composable functions that are added with [addComposableResources]
 * as "resources" and refers to these as "composable resources". Under the older view system, an xml resource would be loaded
 * by inflating it and this would then become an instance of the layout. The CRM applies a similar pattern whereby a composable
 * resource will be selected and called upon to render the actual UI element. Before calling on the composable resource, the
 * CRM creates a structure that it tracks about the composable that will be created. It refers to this structure as a
 * "composable instance".
 *
 * A composable resource is encapsulated in the [ComposableResource] class while a composable instance is encapsulated in
 * a [ComposableInstance] class. When a composable instance is reendered (composed or recomposed), it remains in the
 * navigation stack as long as the screen on which it appears is still "alive". When you navigate to a screen that
 * will have a composable instance generated for it and then navigate forward to another screen, the screen that you
 * are navigating away from still remains "alive". When you navigate back to a previous screen, the current screen's
 * composable instance is removed from the navigation stack.
 *
 * An entire screen can be made up of a single composable instance or made up of multiple children composable instances.
 *
 * A composable resource is generally meant to act as a template for either an entire screen or as a partial screen. For
 * example, consider a phone app that shows a screen that shows a list of pets. When the user clicks on a pet, they
 * navigate to another screen that shows detailed information about the pet. The list screen can be created from a single
 * composable resource called "PetsList" and the pet details screen can be created from another composable resource
 * called "PetDetails". If the same app were to run on a tablet, instead of two separate screens, the developer might
 * opt for just a single screen that shows both the list and the details with the list in the left pane and the details
 * in the right pane. Using the CRM, you can use the exact same composable resources for both a phone and a tablet but
 * create a special tablet composable resource that combines the list and details. The CRM will automatically pick
 * this composable resource when it discovers that the app is running on a tablet. In essence, this is the same
 * selection behavior that Android uses with the old xml resource layouts.
 *
 * In addition to selecting the correction composable resource, the CRM can optionally create a viewmodel that
 * can be assigned to the composable instance and manage its lifecycle.
 *
 * The CRM is designed to work with the [Navigation Manager][NavigationManager], which is responsible for executing a request to
 * navigate to a screen, back to the previous screen or return directly to the home screen. Both the CRM and
 * the Navigation Manager work with the composable instances to provide a fluent execution in how screens are
 * created and destroyed and how animated transitions are executed when navigating to and from screens.
 */
@SuppressLint("StaticFieldLeak")
open class ComposableResourceManager {

    lateinit var navMan: NavigationManager

    private lateinit var ctx: Context
    private lateinit var cfg: Configuration

    private val composableResources = mutableListOf<ComposableResource>()
    private val defaultComposableResources = mutableListOf<ComposableResource>()

    // The cached composables are only temporarily cached during a configuration change. An item
    // is removed from the cache once it's resource id has been resolved during the config changes.
    private val cachedComposableInstances = mutableMapOf<String, MutableMap<String, ComposableInstance>>()

    /**
     * Sets the context that the CRM uses.
     *
     * IMPORTANT: Only set this context using the Application context when the
     * app starts up. Don't use any other context, otherwise you will end up with
     * a memory leak.
     *
     * @param ctx The context that the CRM will use.
     *
     */
    fun setContext(ctx: Context) {
        this.ctx = ctx
        this.cfg = ctx.resources.configuration
        ScreenUtils.setContext(ctx)
    }

    /**
     * Responds to device configuration changes.
     *
     * This should be called from the main activity's onDestroy function. When a configuration
     * change occurs, all the children composable instances on all screens are removed from their
     * parent [composable instance][ComposableInstance]. When the activity is recreated, all the root composable instances
     * (the ones that make up each screen) will be recomposed. This may result in a completely different
     * composable resource being selected for each screen, depending on the type of configuration change
     * and what composable resource (if any) is setup to be used under the new configuration.
     *
     * When a configuration change occurs, all the children composable instances on each screen is
     * temporarily cached internally. When a screen is recomposed, if the new screen requires a composable
     * resource for one of its children and a composable instance of that type is in the cache and was
     * previously assigned to the same screen, it will be reused, otherwise it will be removed from the
     * cache as soon as the user moves to a different screen. For example, if the current screen is displayed
     * in landscape mode showing a list in a pane on the left and a details pane on the right is then rotated
     * to portrait mode but now only shows just the list, the composable instance for the list will be reused
     * and the composable instance for the details pane will be removed as soon as the user navigates to a
     * different screen. Should the user rotate the device to landscape mode again without navigating to a
     * different screen, the composable instance for the details pane will be reused.
     *
     */
    fun onConfigurationChanged() {
        this.cfg = this.ctx.resources.configuration

        navMan.navStack.forEach { parentComposable ->
            parentComposable.selectedResourceId = null

            // Cache all children composables and then remove them from their parent. This will get re-added
            // when the parent composable is recomposed.

            if (parentComposable.composables.isNotEmpty()) {
                var cachedChildren = cachedComposableInstances[parentComposable.id]

                if (cachedChildren == null) {
                    cachedChildren = mutableMapOf()
                    cachedComposableInstances[parentComposable.id] = cachedChildren
                }

                for (i in parentComposable.composables.lastIndex downTo 0) {
                    val childComposable = parentComposable.composables[i]
                    // Clear the selected resource id. This will force an updated resource to
                    // be selected when the composable is recomposed.
                    childComposable.selectedResourceId = null
                    cachedChildren[childComposable.id] = childComposable
                    parentComposable.composables.removeAt(i)
                }
            }
        }
    }

    /**
     * Adds a list of composable resources that the Composable Resource Manager will manage.
     *
     * @param composables A list of one or more composable resources. This function should
     * only be called once in your app and the list provided will be the only list used.
     *
     * For every resource added that has configuration qualifiers defined, a default resource
     * must be provided that contains no qualifiers. For example, if you provide a composable
     * resource with its composableId set to "PetsList" and set its
     * [screenOrientation][ComposableResource.screenOrientation] property
     * to ScreenOrientation.Landscape, you must also provide a composable resource with its
     * composableId also set to "PetsList" with none of its qualifiers set.
     *
     * @exception Exception An exception is thrown if a default composable resource is not provided for
     * any resource that specifies qualifiers.
     */
    fun addComposableResources(composables: List<ComposableResource>) {

        // Check that every composable resource has a default, meaning that it has no qualifiers.
        val resourceIds = mutableSetOf<String>()

        composables.forEach {
            it.id = createId()
            resourceIds.add(it.resourceId)
        }

        resourceIds.forEach { id ->
            val defaultRes = composables.firstOrNull { resource ->
                (resource.resourceId == id) && resourceHasNoQualifiers(resource)
            } ?: throw Exception(
                "No default composable resource provided with the id: $id. Did you forget to add the default " +
                        "composable resource when calling addComposableResources?"
            )

            defaultComposableResources.add(defaultRes)
        }

        // If the onCreateViewmodel has been provided, check that the viewmodelClass has also been provided.
        val resourceWithMissingViewModel = composables.firstOrNull { (it.onCreateViewmodel != null) && (it.viewmodelClass == null) }

        if (resourceWithMissingViewModel != null) {
            throw Exception(
                "The resource composable with the reourceId '" + resourceWithMissingViewModel.resourceId +
                        "' has set its onCreateViewmodel property but did not set the viewmodelClass property. Please set the viewmodelClass property."
            )
        }

        // Check that a resource hasn't been accidentally added more than once for the same id.
        composables.forEach { c ->
            val index = composables.indexOfFirst {
                (c.resourceId == it.resourceId) && (c.id != it.id) &&
                        (c.mcc == it.mcc) &&
                        (c.mnc == it.mnc) &&
                        (c.languageAndRegion == it.languageAndRegion) &&
                        (c.layoutDirection == it.layoutDirection) &&
                        (c.smallestWidthInDp == it.smallestWidthInDp) &&
                        (c.availableWidthInDp == it.availableWidthInDp) &&
                        (c.availableHeightInDp == it.availableHeightInDp) &&
                        (c.screenSize == it.screenSize) &&
                        (c.screenAspect == it.screenAspect) &&
                        (c.roundScreen == it.roundScreen) &&
                        (c.wideColorGamut == it.wideColorGamut) &&
                        (c.highDynamicRange == it.highDynamicRange) &&
                        (c.screenOrientation == it.screenOrientation) &&
                        (c.uiMode == it.uiMode) &&
                        (c.nightMode == it.nightMode) &&
                        (c.screenPixelDensityDpi == it.screenPixelDensityDpi) &&
                        (c.touchScreenType == it.touchScreenType) &&
                        (c.keyboardAvailability == it.keyboardAvailability) &&
                        (c.primaryTextInputMethod == it.primaryTextInputMethod) &&
                        (c.navigationKeyAvailability == it.navigationKeyAvailability) &&
                        (c.primaryNonTouchNavigationMethod == it.primaryNonTouchNavigationMethod) &&
                        (c.platformVersion == it.platformVersion)
            }

            if (index != -1) {
                throw Exception(
                    "A duplicate composable resource has been added using addComposableResources for the resource with the resourceId: '"
                            + c.resourceId + "' added at index: " + index +
                            ". Remove the duplicate resource. No two resources can have the exact same qualifiers set to the same values."
                )
            }
        }

        composableResources.addAll(composables)
    }


    /**
     * Renders a child [composable instance][ComposableInstance]. The child composable instance will be rendered into
     * the parent composable instance.
     *
     * @param parentComposableId If the composable instance that is to be rendered is the root composable on a screen, this
     * is its id. A root commposable had to have been created previously when the [createRootComposableInstance] function was called, in which
     * case the ComposableInstance.id property will have been automatically created.
     *
     * @param composableResId Identifies the composable resource to use to create a composable instance that will be rendered.
     * Whether a resource will be selected and used to create a composable instance depends on whether the childComposableId
     * parameter is specified and whether a child composable instance already exists for the parent composable instance.
     *
     * @param childComposableId The id used to identify the child composable instance. A check is first made to see whether a
     * composable instance for the child already exists in a temporary cache and used if it does. Children composable
     * instances are temporarily cached whenever a device configuration change occurs, such as changing the device's
     * orientation. A configuration change will recompose all the screens. The temporarily cached children are reused if
     * a screen with the same parent is recomposed. Any unused cached children instances remain in the cache until the
     * user navigates back to a previous screen or to the home screen.
     *
     * @param p Any data that needs to be passed to the composable instance.
     */
    @Composable
    fun RenderChildComposable(parentComposableId: String, composableResId: String? = null, childComposableId: String, p: Any? = null) {
        RenderComposableInstance(
            parentComposableId = parentComposableId,
            composableResId = composableResId,
            childComposableId = childComposableId,
            p = p
        )
    }

    @Composable
    internal fun RenderComposableInstance(parentComposableId: String, composableResId: String? = null, childComposableId: String? = null, p: Any? = null) {

        // Check if the composable already exists. If it's being recomposed, it should exist in the
        // navigation stack.

        var composableInstance: ComposableInstance? = null
        val parentComposableInstance = navMan.getComposableInstanceById(id = parentComposableId)

        if (childComposableId != null) {
            // Check if a child composable instance already exists in the cache. If it does, use that.
            val cachedChildren = cachedComposableInstances[parentComposableId]

            if (cachedChildren != null) {
                composableInstance = cachedChildren[childComposableId]

                if (composableInstance != null) {
                    parentComposableInstance.composables.add(composableInstance)

                    // Remove the composable instance from the cache.
                    cachedChildren.remove(childComposableId)
                }
            }

            if (composableInstance == null) {
                composableInstance = parentComposableInstance.composables.firstOrNull { it.id == childComposableId }
            }

            if (composableInstance == null) {
                // Create a new composable instance.

                val _onUpdate = MutableLiveData(0)
                val onUpdate: LiveData<Int> = _onUpdate

                val _onCloseScreen = MutableLiveData(false)
                val onCloseScreen: LiveData<Boolean> = _onCloseScreen

                composableInstance = ComposableInstance(
                    id = childComposableId,
                    parentId = parentComposableId,
                    composableResId = composableResId!!,
                    parameters = p,
                    _onUpdate = _onUpdate,
                    onUpdate = onUpdate,
                    _onCloseScreen = _onCloseScreen,
                    onCloseScreen = onCloseScreen
                )

                parentComposableInstance.composables.add(composableInstance)
            }
        } else {
            composableInstance = parentComposableInstance
        }

        val composableResource: ComposableResource

        if (composableInstance.selectedResourceId == null) {
            composableResource = selectComposableResource(composableResId = composableInstance.composableResId)
            composableInstance.selectedResourceId = composableResource.id

            // Create either a new viewmodel if the composable instance doesn't have one, or create a new one if the
            // viewmodel class changes. Or remove the viewmodel if no viewmodel exists but the composable instance had
            // one previously.
            if ((composableResource.viewmodelClass != null) &&
                ((composableInstance.viewmodel == null) || ((composableInstance.viewmodel != null) && !composableResource.viewmodelClass.isInstance
                    (composableInstance.viewmodel)))
            ) {
                if (composableResource.onCreateViewmodel != null) {
                    composableInstance.viewmodel = composableResource.onCreateViewmodel.invoke()
                } else {
                    composableInstance.viewmodel = composableResource.viewmodelClass.newInstance() as ViewModel
                }

            } else if ((composableResource.viewmodelClass == null) && (composableInstance.viewmodel != null)) {
                composableInstance.viewmodel = null
            }
        } else {
            val selecteResId = composableInstance.selectedResourceId

            // Find the composable resource that was previously selected.
            composableResource = composableResources.find {
                it.id == selecteResId
            } as ComposableResource
        }

        if (p != null) {
            composableInstance.parameters = p
        }

        // Render the composable.
        composableResource.onRender(composableInstance)
    }

    /**
     * Renders a composable.
     *
     * @param composableInstance If the [isTerminated][ComposableInstance.isTerminated] property is set to true, the selectedResourceId
     * will be used to select the composable resource and render the composable instance with that
     * resource. If isTerminated is false, the composable instance is rendered as it normally would be
     * using only the parentComposableId.
     */
    @Composable
    fun RenderComposable(composableInstance: ComposableInstance) {
        if (composableInstance.isTerminated) {
            val composableResource = composableResources.first { it.id == composableInstance.selectedResourceId }
            composableResource.onRender(composableInstance)
        } else {
            RenderComposableInstance(parentComposableId = composableInstance.id)
        }
    }


    /**
     * Returns the composable resource that will be used for the specified [composable instance][ComposableInstance]
     *
     * @param composableInstance The composable instance to use.
     *
     * @return Returns the composable resource that would be used if the composable instance were rendered on the
     * screen for the current device configuration.
     */
    fun getComposableResourceForComposableInstance(composableInstance: ComposableInstance): ComposableResource {

        val composableResource: ComposableResource = if (composableInstance.selectedResourceId == null) {
            selectComposableResource(composableResId = composableInstance.composableResId)
        } else {
            val selecteResId = composableInstance.selectedResourceId

            // Find the composable resource that was previously selected.
            composableResources.find {
                it.id == selecteResId
            } as ComposableResource
        }

        return composableResource
    }

    /**
     * Finds the child [composable instance][ComposableInstance] for the specified parent composable instance.
     *
     * @param parentComposableInstance The composable instance of the parent.
     *
     * @param childComposableId The id of the child composable instance to find. If set to null,
     * the childComposableResourceId parameter will be used to see if a child exists for the parent
     * that has the resource id and that child will be returned.
     *
     * @param childComposableResourceId If no child composable instance can be found when a childComposableId
     * is specified, a child is searched for using the resource id of the child.
     *
     * @return The child composable instance is returned if one exists, otherwise null is returned.
     */
    fun getChildComposableInstance(
        parentComposableInstance: ComposableInstance,
        childComposableId: String? = null,
        childComposableResourceId: String
    ): ComposableInstance? {

        val parentComposable: ComposableInstance = if (parentComposableInstance.isRoot) {
            parentComposableInstance
        } else {
            navMan.getRootComposableInstanceById(parentComposableInstance.id)
        }

        var dstComposable: ComposableInstance? = null

        if (childComposableId != null) {
            dstComposable = parentComposable.composables.firstOrNull { it.id == childComposableId }
        }

        if (dstComposable == null) {
            // Check if a composable exists on the current screen that uses the destination composable resource.
            dstComposable = parentComposable.composables.firstOrNull { it.composableResId == childComposableResourceId }
        }

        return dstComposable
    }

    /**
     * Updates an existing child composable instance or navigates to a new screen using the specified
     * child composable resource. This is essentially a shortcut call to [getChildComposableInstance] and
     * [createRootComposableInstance]. First a call will be made to getChildComposableInstance to retrieve the child if it
     * exists. If it exists, the child will be updated. If no child exists, the goto function will
     * be called.
     *
     * @param parentComposableInstance The parent composable instance that the child may or may not belong to.
     *
     * @param childComposableId The id of the child when getChildComposableInstance is called.
     *
     * @param childComposableResourceId The id of the composable resource when [getChildComposableInstance] is called.
     *
     * @param fullscreenComposableResourceId If the CRM determines that no child composable instance exists on the
     * current screen, it will navigate to a new screen using the composable resource specified by this parameter.
     *
     * @param p If no child composable instance exists for the specified parent, [createRootComposableInstance] will be called and
     * the p parameter will be passed to the new screen. If a child composable instance exists for the parent,
     * the p parameter is ignored. If the client needs to update a child composable that is on the same screen,
     * it should call getChildComposableInstance first and then update the child's parameters before calling
     * updateOrGoto.
     *
     * @param cacheComposable If set to true and the CRM navigates to a new screen, the new screen will be cached
     * if this parameter is set to true.
     */
    fun updateOrGoto(
        parentComposableInstance: ComposableInstance,
        childComposableId: String? = null,
        childComposableResourceId: String,
        fullscreenComposableResourceId: String,
        p: Any? = null,
        cacheComposable: Boolean = false
    ) {
        val childComposableInstance = notifyChildComposableInstanceOfUpdate(
            parentComposableInstance = parentComposableInstance,
            childComposableId = childComposableId,
            childComposableResourceId = childComposableResourceId,
            p = p
        )

        if (childComposableInstance == null) {
            navMan.goto(composableResId = fullscreenComposableResourceId, p = p, cacheComposable = cacheComposable)
        }
    }

    /**
     * Notifies a [composable instance][ComposableInstance] of an update.
     *
     * This function only generates a notification. It is the responsibility of the calling client
     * to have updated any data that the composable instance needs.
     *
     * @param composableInstance The composable instance that will be notified.
     */
    fun notifyComposableInstanceOfUpdate(composableInstance: ComposableInstance) {
        composableInstance._onUpdate?.value = (0..1_000_000).random()
    }

    /**
     * Notifies a child [composable instance][ComposableInstance] of any updates.
     *
     * @param parentComposableInstance The parent composable instance that the child composable instance belongs to.
     *
     * @param childComposableId The id of the child composable instance. If this is provided, a search is first made
     * to see if a child composable instance exists. If the id is not specified or the child with the id cannot be
     * found, the childComposableResourceId will be used to search for the child.
     *
     * @param childComposableResourceId The composable resource id of the child composable instance. If multiple children
     * exist on the screen with the same composable resource id and none can be found with the childComposableId, then
     * the first child with the composable resource id will be notified of the update.
     *
     * @return The child composable instance is returned if it exists, otherwise null is returned.
     */
    fun notifyChildComposableInstanceOfUpdate(
        parentComposableInstance: ComposableInstance,
        childComposableId: String? = null,
        childComposableResourceId: String,
        p: Any? = null
    ): ComposableInstance? {
        val childComposableInstance = getChildComposableInstance(
            parentComposableInstance = parentComposableInstance,
            childComposableId = childComposableId,
            childComposableResourceId = childComposableResourceId
        )

        if (childComposableInstance != null) {
            if (p != null) {
                childComposableInstance.parameters = p
            }

            notifyComposableOfUpdate(childComposableInstance)
        }

        return childComposableInstance
    }

    /**
     * Selects the [composable resource][ComposableResource] that will be used to render a composable instance.
     *
     * @param composableResId The composable resource id. Many composable resources can be defined
     * using addComposableResources but the one selected for rendering a composable instance depends
     * on the current device configuration.
     *
     * @return The selected composable resource will be returned.
     */
    fun selectComposableResource(composableResId: String): ComposableResource {

        val resourcesFiltered = composableResources.filter { it.resourceId == composableResId }.toMutableList()

        var i = 0

        // Eliminate resources that have qualifiers that contradict the current system configuration.
        // The screenPixelDensity is not eliminated because if multiple values exist, the
        // best one will be selected later on, even one where the resource density is much larger than
        // the device's screen density, if no better resource exists closer to the screen's
        // density.

        while (i < resourcesFiltered.size) {
            val resource = resourcesFiltered[i]

            if (mccAndMNCUnsupported(resource) ||
                languageAndRegionUnsupported(resource) ||
                layoutDirectionUnsupported(resource) ||
                smallestWidthUnsupported(resource) ||
                availableWidthUnsupported(resource) ||
                availableHeightUnsupported(resource) ||
                screenSizeUnsupported(resource) ||
                screenAspectUnsupported(resource) ||
                roundScreenUnsupported(resource) ||
                wideColorGamutUnsupported(resource) ||
                highDynamicRangeUnsupported(resource) ||
                screenOrientationUnsupported(resource) ||
                uiModeUnsupported(resource) ||
                nightModeUnsupported(resource) ||
                touchScreenTypeUnsupported(resource) ||
                keyboardAvailibilityUnsupported(resource) ||
                primaryTextInputMethodUnsupported(resource) ||
                navigationKeyAvailibilityUnsupported(resource) ||
                primaryNonTouchNavigationMethodUnsupported(resource) ||
                platformVersionUnsupported(resource)
            ) {
                resourcesFiltered.removeAt(i)
                continue
            }

            i++
        }

        val qualifiersToCheck = mutableSetOf<QualifierConfiguration>()

        // Create a set that contains all the qualifiers for those resources that are left over.
        // Important: The qualifiers are added to the collection in order of precedence with MCC/MNC being
        // the highest precedent and platform verion being the lowest.

        resourcesFiltered.forEach { r ->
            if (r.mcc != null) qualifiersToCheck.add(QualifierConfiguration.MCCAndMNC)
            if (r.languageAndRegion != null) qualifiersToCheck.add(QualifierConfiguration.LanguageAndRegion)
            if (r.layoutDirection != null) qualifiersToCheck.add(QualifierConfiguration.LayoutDirection)
            if (r.smallestWidthInDp != null) qualifiersToCheck.add(QualifierConfiguration.SmallestWidth)
            if (r.availableWidthInDp != null) qualifiersToCheck.add(QualifierConfiguration.AvailableWidth)
            if (r.availableHeightInDp != null) qualifiersToCheck.add(QualifierConfiguration.AvailableHeight)
            if (r.screenSize != null) qualifiersToCheck.add(QualifierConfiguration.ScreenSize)
            if (r.screenAspect != null) qualifiersToCheck.add(QualifierConfiguration.ScreenAspect)
            if (r.roundScreen != null) qualifiersToCheck.add(QualifierConfiguration.RoundScreen)
            if (r.wideColorGamut != null) qualifiersToCheck.add(QualifierConfiguration.WideColorGamut)
            if (r.highDynamicRange != null) qualifiersToCheck.add(QualifierConfiguration.HighDynamicRange)
            if (r.screenOrientation != null) qualifiersToCheck.add(QualifierConfiguration.ScreenOrientation)
            if (r.uiMode != null) qualifiersToCheck.add(QualifierConfiguration.UIMode)
            if (r.nightMode != null) qualifiersToCheck.add(QualifierConfiguration.NightMode)
            if (r.screenPixelDensityDpi != null) qualifiersToCheck.add(QualifierConfiguration.ScreenPixelDensityDpi)
            if (r.touchScreenType != null) qualifiersToCheck.add(QualifierConfiguration.TouchScreenType)
            if (r.keyboardAvailability != null) qualifiersToCheck.add(QualifierConfiguration.KeyboardAvailibility)
            if (r.primaryTextInputMethod != null) qualifiersToCheck.add(QualifierConfiguration.PrimaryTextInputMethod)
            if (r.navigationKeyAvailability != null) qualifiersToCheck.add(QualifierConfiguration.NavigationKeyAvailibility)
            if (r.primaryNonTouchNavigationMethod != null) qualifiersToCheck.add(QualifierConfiguration.PrimaryNonTouchNavigationMethod)
            if (r.platformVersion != null) qualifiersToCheck.add(QualifierConfiguration.PlatformVersion)
        }

        // Eliminate those resources that don't support the qualifiers.
        qualifiersToCheck.forEach { q ->
            var c = 0

            while (c < resourcesFiltered.size) {
                val r = resourcesFiltered[c]

                if (unsupportedQualifier(r, q, resourcesFiltered, c)) {
                    continue
                }

                c++
            }
        }

        // Some of the qualifiers can appear multiple times such as smallestWidth, availableWidth, availableHeight, screenSize and
        // screenPixelDensity. These will be reduced to the best one. It is important however that the processing of these
        // qualifiers occur in the order of the qualifier's precedence to ensure that the final selected composable resource
        // would end up being the same one if the app was built using the older xml view system.

        // Select resources with best sizes for smallestWidth, availableWidth and availableHeight
        selectResourceWithClosestSize(resourcesFiltered, QualifierConfiguration.SmallestWidth, cfg.smallestScreenWidthDp)
        selectResourceWithClosestSize(resourcesFiltered, QualifierConfiguration.AvailableWidth, ScreenUtils.availableScreenSizeInDp().first.toInt())
        selectResourceWithClosestSize(resourcesFiltered, QualifierConfiguration.AvailableHeight, ScreenUtils.availableScreenSizeInDp().second.toInt())

        // Select the resource with the best screen size.
        selectResourceWithBestScreenSize(resourcesFiltered)

        // Select the resource with the best screen pixel density.
        selectResourceWithBestScreenPixelDensity(resourcesFiltered)

        if (resourcesFiltered.isNotEmpty()) {
            // There really should only be one item in the list at this point.
            return resourcesFiltered[0]
        }

        // If no resource was selected, return the default resource.
        return defaultComposableResources.first { it.resourceId == composableResId }
    }

    /**
     * Returns the parent composable instance.
     *
     * @param composableInstance This should be a reference to a child composable instance. But if it is
     * actually the parent instance itself, this value of this parameter will be returned.
     */
    fun getParentComposableInstance(composableInstance: ComposableInstance): ComposableInstance {
        if (composableInstance.isRoot) {
            return composableInstance
        }

        return getComposableInstanceById(composableInstance.parentId!!) as ComposableInstance
    }

    /**
     * Returns the composable instance that has the specified id.
     *
     * @param id The id of the composable instance.
     */
    fun getComposableInstanceById(id: String): ComposableInstance? {
        var composableInstance = getComposableInstanceById(id, navMan.navStack)

        if (composableInstance != null) {
            return composableInstance
        }

        composableInstance = getComposableInstanceById(id, navMan.navCache)

        if (composableInstance != null) {
            return composableInstance
        }

        return null
    }

    private fun getComposableInstanceById(id: String, composables: List<ComposableInstance>): ComposableInstance? {
        for (i in 0 until composables.lastIndex) {
            val rootComposable = composables[i]

            if (rootComposable.id == id) {
                return rootComposable
            }

            val childComposable = rootComposable.composables.firstOrNull { it.id == id }

            if (childComposable != null) {
                return childComposable
            }
        }

        return null
    }

    /**
     * Creates a root [composable instance][ComposableInstance].
     *
     * @param composableInstanceId If specified, make sure that this id is unique throughout your app. If the
     * composable instance is to be cached when [NavigationManager.goto] is called, this is the id that can be used
     * to navigate to the cached composable instance. If this id is null, a random id will be created.
     *
     * @param composableResId The id of the [composable resource][ComposableResource] that will be used to select
     * which resource will be used to create a composable instance. The id that you provide
     * must be the same id that is provided by the [resourceId][ComposableResource.resourceId] parameter of a ComposableResource
     * added with [addComposableResources].
     *
     * If a viewmodel is specified by the composable resource, it will be created at this
     * point and associated with the composable instance.
     *
     * @param p Any parameter data that needs to be passed to the composable instance.
     */
    internal fun createRootComposableInstance(
        composableInstanceId: String? = null,
        composableResId: String,
        p: Any? = null,
        deepLink: DeepLink? = null
    ): ComposableInstance {

        // Create a new composable that will serve as the root composable of the screen
        // that will be navigated to.

        val _onUpdate = MutableLiveData(0)
        val onUpdate: LiveData<Int> = _onUpdate

        val _onCloseScreen = MutableLiveData(false)
        val onCloseScreen: LiveData<Boolean> = _onCloseScreen

        val composableInstance = ComposableInstance(
            id = composableInstanceId ?: createId(),
            composableResId = composableResId,
            parameters = p,
            deepLink = deepLink,
            _onUpdate = _onUpdate,
            onUpdate = onUpdate,
            _onCloseScreen = _onCloseScreen,
            onCloseScreen = onCloseScreen,
            isRoot = true
        )

        val composableResource = composableResources.firstOrNull { it.resourceId == composableResId }
            ?: throw Exception(
                "No composable resource found with the id: '" + composableResId + "'. Did you forget to add it when you called " +
                        "ComposableResourceManager.addComposableResources?"
            )

        if (composableResource.viewmodelClass != null) {
            composableInstance.viewmodel = composableResource.viewmodelClass.newInstance() as ViewModel
        }

        return composableInstance
    }

    internal fun onNavigationBackOrToHomeScreen(composableInstance: ComposableInstance) {
        cachedComposableInstances.remove(composableInstance.id)
    }

    private fun resourceHasNoQualifiers(r: ComposableResource): Boolean {
        return (r.mcc == null) && (r.mnc == null) && (r.languageAndRegion == null) && (r.layoutDirection == null) &&
                (r.smallestWidthInDp == null) && (r.availableWidthInDp == null) && (r.availableHeightInDp == null) &&
                (r.screenSize == null) && (r.screenAspect == null) && (r.roundScreen == null) &&
                (r.wideColorGamut == null) && (r.highDynamicRange == null) && (r.screenOrientation == null) &&
                (r.uiMode == null) && (r.nightMode == null) && (r.screenPixelDensityDpi == null) &&
                (r.touchScreenType == null) && (r.keyboardAvailability == null) && (r.primaryTextInputMethod == null) &&
                (r.navigationKeyAvailability == null) && (r.primaryNonTouchNavigationMethod == null) &&
                (r.platformVersion == null)
    }

    private fun createId(): String {
        return (0..1_000_000).random().toString()
    }

    private fun notifyComposableOfUpdate(composableInstance: ComposableInstance) {
        composableInstance._onUpdate?.value = (0..1_000_000).random()
    }

    private fun selectResourceWithBestScreenPixelDensity(resources: MutableList<ComposableResource>) {
        var c = 0
        var prevResDensity: Int? = null
        var prevIndex = 0
        var useAnyDensity = false
        var useNoDensity = false

        while (c < resources.size) {
            val resDensity = resources[c].screenPixelDensityDpi

            if (resDensity != null) {
                if (prevResDensity == null) {
                    prevResDensity = resDensity
                    prevIndex = c
                    c++
                    continue
                }

                var useRes = false

                if (resDensity == ScreenPixelDensityDpi.AnyDPI) {
                    useRes = true
                    useAnyDensity = true
                } else if ((resDensity == ScreenPixelDensityDpi.NODPI) && !useAnyDensity) {
                    useRes = true
                    useNoDensity = true
                } else if ((ctx.resources.displayMetrics.densityDpi - resDensity < prevResDensity) && !useAnyDensity && !useNoDensity) {
                    useRes = true
                }

                if (useRes) {
                    prevResDensity = resDensity
                    resources.removeAt(prevIndex)
                    prevIndex = c - 1
                } else {
                    resources.removeAt(c)
                }
            } else {
                c++
            }
        }
    }

    private fun selectResourceWithBestScreenSize(resources: MutableList<ComposableResource>) {
        var c = 0
        var prevResSize: ScreenSize? = null
        var prevIndex = 0

        while (c < resources.size) {
            val resScreenSize = resources[c].screenSize

            if (resScreenSize != null) {
                if (prevResSize == null) {
                    prevResSize = resScreenSize
                    prevIndex = c
                    c++
                    continue
                }

                if (deviceScreenSizeVal() - resourceScreenSizeVal(resScreenSize) < resourceScreenSizeVal(prevResSize)) {
                    prevResSize = resScreenSize
                    resources.removeAt(prevIndex)
                    prevIndex = c - 1
                } else {
                    resources.removeAt(c)
                }
            } else {
                c++
            }
        }
    }

    private fun deviceScreenSizeVal(): Int {
        return when (true) {
            deviceScreenSizeIsSmall -> SCREENLAYOUT_SIZE_SMALL
            deviceScreenSizeIsLarge -> SCREENLAYOUT_SIZE_LARGE
            deviceScreenSizeIsXLarge -> SCREENLAYOUT_SIZE_XLARGE
            else -> SCREENLAYOUT_SIZE_NORMAL
        }
    }

    private fun resourceScreenSizeVal(screenSize: ScreenSize): Int {
        return when (screenSize) {
            ScreenSize.Small -> SCREENLAYOUT_SIZE_SMALL
            ScreenSize.Large -> SCREENLAYOUT_SIZE_LARGE
            ScreenSize.XLarge -> SCREENLAYOUT_SIZE_XLARGE
            else -> SCREENLAYOUT_SIZE_NORMAL
        }
    }

    private val deviceScreenSizeIsSmall: Boolean
        get() {
            return (cfg.screenLayout and SCREENLAYOUT_SIZE_SMALL > 0)
        }

    private val deviceScreenSizeIsNormal: Boolean
        get() {
            return (cfg.screenLayout and SCREENLAYOUT_SIZE_NORMAL > 0)
        }

    private val deviceScreenSizeIsLarge: Boolean
        get() {
            return (cfg.screenLayout and SCREENLAYOUT_SIZE_LARGE > 0)
        }

    private val deviceScreenSizeIsXLarge: Boolean
        get() {
            return (cfg.screenLayout and SCREENLAYOUT_SIZE_XLARGE > 0)
        }

    private fun selectResourceWithClosestSize(resources: MutableList<ComposableResource>, qualifier: QualifierConfiguration, deviceSize: Int) {
        var c = 0
        var prevResSize: Int? = null
        var prevIndex = 0

        while (c < resources.size) {
            val res = resources[c]

            val resSize = when (qualifier) {
                QualifierConfiguration.SmallestWidth -> res.smallestWidthInDp
                QualifierConfiguration.AvailableWidth -> res.availableWidthInDp
                else -> res.availableHeightInDp
            }

            if ((resSize != null) && (resSize <= deviceSize)) {
                if (prevResSize == null) {
                    prevResSize = resSize
                    prevIndex = c
                    c++
                    continue
                }

                if (deviceSize - resSize < prevResSize) {
                    prevResSize = deviceSize
                    resources.removeAt(prevIndex)
                    prevIndex = c - 1
                } else {
                    resources.removeAt(c)
                }
            } else {
                c++
            }
        }
    }

    private fun unsupportedQualifier(
        composableResource: ComposableResource,
        qualifier: QualifierConfiguration,
        resources: MutableList<ComposableResource>,
        index: Int
    ): Boolean {
        return if (when (qualifier) {
                QualifierConfiguration.MCCAndMNC -> composableResource.mcc as Any?
                QualifierConfiguration.LanguageAndRegion -> composableResource.languageAndRegion
                QualifierConfiguration.LayoutDirection -> composableResource.layoutDirection
                QualifierConfiguration.SmallestWidth -> composableResource.smallestWidthInDp
                QualifierConfiguration.AvailableWidth -> composableResource.availableWidthInDp
                QualifierConfiguration.AvailableHeight -> composableResource.availableHeightInDp
                QualifierConfiguration.ScreenSize -> composableResource.screenSize
                QualifierConfiguration.ScreenAspect -> composableResource.screenAspect
                QualifierConfiguration.RoundScreen -> composableResource.roundScreen
                QualifierConfiguration.WideColorGamut -> composableResource.wideColorGamut
                QualifierConfiguration.HighDynamicRange -> composableResource.highDynamicRange
                QualifierConfiguration.ScreenOrientation -> composableResource.screenOrientation
                QualifierConfiguration.UIMode -> composableResource.uiMode
                QualifierConfiguration.NightMode -> composableResource.nightMode
                QualifierConfiguration.ScreenPixelDensityDpi -> composableResource.screenPixelDensityDpi
                QualifierConfiguration.TouchScreenType -> composableResource.touchScreenType
                QualifierConfiguration.KeyboardAvailibility -> composableResource.keyboardAvailability
                QualifierConfiguration.PrimaryTextInputMethod -> composableResource.primaryTextInputMethod
                QualifierConfiguration.NavigationKeyAvailibility -> composableResource.navigationKeyAvailability
                QualifierConfiguration.PrimaryNonTouchNavigationMethod -> composableResource.primaryNonTouchNavigationMethod
                else -> composableResource.platformVersion
            } == null
        ) {
            resources.removeAt(index)
            true
        } else false
    }

    private fun mccAndMNCUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.mcc == null) return false
        if (composableResource.mcc != cfg.mcc) return true
        if ((composableResource.mnc != null) && (composableResource.mnc != cfg.mnc)) return true

        return false
    }

    private fun languageAndRegionUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.languageAndRegion.isNullOrEmpty()) {
            return false
        }

        val langRegionQualifier = composableResource.languageAndRegion.lowercase()

        // TODO: Add support for BCP 47 language tags.
        if (langRegionQualifier.startsWith("b+")) {
            return true
        }

        val langRegQ = langRegionQualifier.split("-").toMutableList()
        val langRegionDevice = Locale.getDefault().toString().lowercase().split("_")

        if (langRegQ.size == 2) {
            // Strip off the "r" that precedes the region code.
            langRegQ[1] = langRegQ[1].substring(1)
        }

        if (langRegQ[0] != langRegionDevice[0]) {
            return true
        }

        if ((langRegQ.size == 2) && (langRegQ[1] != langRegionDevice[1])) {
            return true
        }

        return false
    }

    private fun layoutDirectionUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.layoutDirection == null) {
            return false
        }

        if (((composableResource.layoutDirection == LayoutDirection.LeftToRight) && (cfg.layoutDirection == SCREENLAYOUT_LAYOUTDIR_LTR)) ||
            ((composableResource.layoutDirection == LayoutDirection.RightToLeft) && (cfg.layoutDirection == SCREENLAYOUT_LAYOUTDIR_RTL))
        ) {
            return false
        }

        return true
    }

    private fun smallestWidthUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.smallestWidthInDp == null) {
            return false
        }

        if (cfg.smallestScreenWidthDp >= composableResource.smallestWidthInDp!!) {
            return false
        }

        return true
    }

    private fun availableWidthUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.availableWidthInDp == null) {
            return false
        }

        val availableWidthInDp = composableResource.availableWidthInDp
        val availableScreenSize = ScreenUtils.availableScreenSizeInDp()

        if (availableScreenSize.first >= availableWidthInDp) {
            return false
        }

        return true
    }

    private fun availableHeightUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.availableHeightInDp == null) {
            return false
        }

        val availableHeightInDp = composableResource.availableHeightInDp as Float
        val availableScreenSize = ScreenUtils.availableScreenSizeInDp()

        if (availableScreenSize.second >= availableHeightInDp) {
            return false
        }

        return true
    }

    private fun screenSizeUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.screenSize == null) {
            return false
        }

        if ((composableResource.screenSize == ScreenSize.Large) && !deviceScreenSizeIsSmall && !deviceScreenSizeIsNormal) {
            return false
        } else if ((composableResource.screenSize == ScreenSize.Normal) && !deviceScreenSizeIsSmall) {
            return false
        } else if ((composableResource.screenSize == ScreenSize.XLarge) && deviceScreenSizeIsXLarge) {
            return false
        } else if (composableResource.screenSize == ScreenSize.Small) {
            return false
        }

        return true
    }

    private fun screenAspectUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.screenAspect == null) {
            return false
        }

        if ((composableResource.screenAspect == ScreenAspect.NotLong) && (cfg.screenLayout and SCREENLAYOUT_LONG_NO > 0)) {
            return false
        } else if ((composableResource.screenAspect == ScreenAspect.Long) && (cfg.screenLayout and SCREENLAYOUT_LONG_YES > 0)) {
            return false
        }

        return true
    }

    private fun roundScreenUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.roundScreen == null) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((composableResource.roundScreen == RoundScreen.Round) && (cfg.screenLayout and SCREENLAYOUT_ROUND_YES > 0)) {
                return false
            } else if ((composableResource.roundScreen == RoundScreen.NotRound) && (cfg.screenLayout and SCREENLAYOUT_ROUND_NO > 0)) {
                return false
            }
        }

        return true
    }

    private fun wideColorGamutUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.wideColorGamut == null) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ((composableResource.wideColorGamut == WideColorGamut.NoWideCG) && (cfg.colorMode and COLOR_MODE_WIDE_COLOR_GAMUT_NO > 0)) {
                return false
            } else if ((composableResource.wideColorGamut == WideColorGamut.WideCG) && (cfg.colorMode and COLOR_MODE_WIDE_COLOR_GAMUT_YES > 0)) {
                return false
            }
        }

        return true
    }

    private fun highDynamicRangeUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.highDynamicRange == null) {
            return false
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if ((composableResource.highDynamicRange == HighDynamicRange.HighDR) && (cfg.colorMode and COLOR_MODE_HDR_YES > 0)) {
                return false
            } else if ((composableResource.highDynamicRange == HighDynamicRange.LowDR) && (cfg.colorMode and COLOR_MODE_HDR_NO > 0)) {
                return false
            }
        }

        return true
    }

    private fun screenOrientationUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.screenOrientation == null) {
            return false
        }

        if ((composableResource.screenOrientation == ScreenOrientation.Portrait) && (cfg.orientation and ORIENTATION_PORTRAIT > 0)) {
            return false
        } else if ((composableResource.screenOrientation == ScreenOrientation.Landscape) && (cfg.orientation and ORIENTATION_LANDSCAPE > 0)) {
            return false
        }

        return true
    }

    private fun uiModeUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.uiMode == null) {
            return false
        }

        if ((composableResource.uiMode == UIMode.Car) && (cfg.uiMode and UI_MODE_TYPE_CAR > 0)) {
            return false
        } else if ((composableResource.uiMode == UIMode.Desk) && (cfg.uiMode and UI_MODE_TYPE_DESK > 0)) {
            return false
        } else if ((composableResource.uiMode == UIMode.Television) && (cfg.uiMode and UI_MODE_TYPE_TELEVISION > 0)) {
            return false
        } else if ((composableResource.uiMode == UIMode.Appliance) && (cfg.uiMode and UI_MODE_TYPE_APPLIANCE > 0)) {
            return false
        } else if ((composableResource.uiMode == UIMode.Watch) && (cfg.uiMode and UI_MODE_TYPE_WATCH > 0)) {
            return false
        } else if ((composableResource.uiMode == UIMode.VRHeadset) && (cfg.uiMode and UI_MODE_TYPE_VR_HEADSET > 0)) {
            return false
        }

        return true
    }

    private fun nightModeUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.nightMode == null) {
            return false
        }

        if ((composableResource.nightMode == NightMode.NotNight) && (cfg.uiMode and UI_MODE_NIGHT_NO > 0)) {
            return false
        } else if ((composableResource.nightMode == NightMode.Night) && (cfg.uiMode and UI_MODE_NIGHT_YES > 0)) {
            return false
        }

        return true
    }

    private fun touchScreenTypeUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.touchScreenType == null) {
            return false
        }

        if ((composableResource.touchScreenType == TouchScreenType.Finger) && (cfg.touchscreen == TOUCHSCREEN_FINGER)) {
            return false
        } else if ((composableResource.touchScreenType == TouchScreenType.NoTouch) && (cfg.touchscreen == TOUCHSCREEN_NOTOUCH)) {
            return false
        }

        return true
    }

    private fun keyboardAvailibilityUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.keyboardAvailability == null) {
            return false
        }

        if (((composableResource.keyboardAvailability == KeyboardAvailability.KeysSoft) ||
                    (composableResource.keyboardAvailability == KeyboardAvailability.KeysExposed)) && (cfg.keyboardHidden == KEYBOARDHIDDEN_NO)
        ) {
            return false
        } else if ((composableResource.keyboardAvailability == KeyboardAvailability.KeysHidden) && (cfg.keyboardHidden == KEYBOARDHIDDEN_YES)) {
            return false
        }

        return true
    }

    private fun primaryTextInputMethodUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.primaryTextInputMethod == null) {
            return false
        }

        if ((composableResource.primaryTextInputMethod == PrimaryTextInputMethod.Qwerty) && (cfg.keyboard == KEYBOARD_QWERTY)) {
            return false
        } else if ((composableResource.primaryTextInputMethod == PrimaryTextInputMethod.NoKeys) && (cfg.keyboard == KEYBOARD_NOKEYS)) {
            return false
        } else if ((composableResource.primaryTextInputMethod == PrimaryTextInputMethod.TwelveKey) && (cfg.keyboard == KEYBOARD_12KEY)) {
            return false
        }

        return true
    }

    private fun navigationKeyAvailibilityUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.navigationKeyAvailability == null) {
            return false
        }

        if ((composableResource.navigationKeyAvailability == NavigationKeyAvailibility.NavExposed) && (cfg.navigationHidden == NAVIGATIONHIDDEN_NO)) {
            return false
        } else if ((composableResource.navigationKeyAvailability == NavigationKeyAvailibility.NavHidden) && (cfg.navigationHidden == NAVIGATIONHIDDEN_YES)) {
            return false
        }

        return true
    }

    private fun primaryNonTouchNavigationMethodUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.primaryNonTouchNavigationMethod == null) {
            return false
        }

        if ((composableResource.primaryNonTouchNavigationMethod == PrimaryNonTouchNavigationMethod.NoNav) && (cfg.navigation == NAVIGATION_NONAV)) {
            return false
        } else if ((composableResource.primaryNonTouchNavigationMethod == PrimaryNonTouchNavigationMethod.DPad) && (cfg.navigation == NAVIGATION_DPAD)) {
            return false
        } else if ((composableResource.primaryNonTouchNavigationMethod == PrimaryNonTouchNavigationMethod.Trackball) && (cfg.navigation == NAVIGATION_TRACKBALL)) {
            return false
        } else if ((composableResource.primaryNonTouchNavigationMethod == PrimaryNonTouchNavigationMethod.Wheel) && (cfg.navigation == NAVIGATION_WHEEL)) {
            return false
        }

        return true
    }

    private fun platformVersionUnsupported(composableResource: ComposableResource): Boolean {
        if (composableResource.platformVersion == null) {
            return false
        }

        return Build.VERSION.SDK_INT < composableResource.platformVersion as Int
    }
}

@SuppressLint("StaticFieldLeak")
lateinit var crm: ComposableResourceManager
