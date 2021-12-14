package dev.wirespec.jetmagic.navigation

import android.annotation.SuppressLint
import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.wirespec.jetmagic.composables.ComposableResourceManager
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableResource
import dev.wirespec.jetmagic.models.DeepLink
import dev.wirespec.jetmagic.models.DeepLinkMap
import dev.wirespec.jetmagic.navigation.NavigateBackOptions.*
import java.net.URI

/**
 * Manages the navigation to and from screens that are created using Jetpack Compose composables.
 *
 * Navigation Manager is intended to replace Android's own Jetpack navigation system to overcome a number of
 * limitations including the lack of support to pass objects between screens and use animated transitions when
 * switching between screens. NavigationManager is not compatible with Android's standard Jetpack Compose Navigation
 * framework.
 *
 * Navigation Manager is the single source of truth for which screen is currently being displayed and tracks the
 * user's navigation path.
 */
class NavigationManager {

    lateinit var crm: ComposableResourceManager
    var activity: Activity? = null
    internal var navStack = mutableListOf<ComposableInstance>()

    // This contains the list of composable resource Ids that are added in the order in which
    // navigation will take place from the starting screen to the last screen.
    internal val deepLinks = mutableListOf<DeepLink>()

    internal val deepLinkMap = mutableListOf<DeepLinkMap>()
    internal var onDeepLinkMatchNotFound: ((uri: URI, queryKeyValues: Map<String, String>) -> List<String>?)? = null

    private val MaxRandomNumber = 1_000_000
    private val screenChangeObservers = mutableListOf<(ComposableInstance) -> Unit>()

    /**
     * Both the Navigation Manager and the ComposableResourceManager have caches that
     * cache composable instances. They should not be confused as they both have
     * separate reasons for caching the composable instances. The Navigation Manager
     * caches those instances that are root composable instances that have been
     * requested by goto to be cached. The ComposableResourceManager on the
     * other hand caches only the children composable resources when a device
     * configuration change takes place and removes them from its cache when
     * a composable instance that uses them is recomposed or the user navigates
     * to the previous screen or to the home screen.
     */
    internal var navCache = mutableListOf<ComposableInstance>()

    private val _onScreenChange = MutableLiveData(0)

    /**
     * The trash is used to temporarily store composable instances that are removed from the navigation
     * stack. When the user clicks the back button, the current screen's root composable instance
     * is stored in the trash. When the screen that the composable instance is assocated with is
     * recomposed, the composable instance will not be available in the navigation stack but the
     * children composable instances on the screen still need access to the original parent composable
     * instance. This is because by default, animations are used when a screen is being removed.
     *
     * When a search is made to obtain the root composable instance in the navigation stack and cannot
     * find it, a search is made in the trash. It should be in the trash and if it isn't an exception
     * will be throw (this really should never happen). Each time the user navigates back to a previous
     * screen or goes directly to the home screen, the trash is emptied. The root composable instance
     * is placed into the trash after the trash has been cleared.
     */
    internal var trash = mutableListOf<ComposableInstance>()

    init {
        /**
         * The last item in the stack is an instance of a ComposableInstance that only serves
         * as a placeholder required for animating from one screen to another one. It must
         * always remain in the collection and be the last item in the collection.
         *
         * IMPORTANT: The id property for the last item must be set to an empty string to
         * identify it as the placeholder.
         */

        navStack.add(ComposableInstance(id = "", composableResId = ""))
    }

    /**
     * Indicates the total number of screens on the stack. This includes the hidden placeholder screen that always
     * appears last in the stack. The stack can include hidden screens which can be created using deep links. A deep
     * link can be setup to render multiple screens in succession and optionally hide all but the last of these screens.
     */
    val navStackCount: Int
        get() {
            return navStack.size
        }

    /**
     * Indicates the total number of screens on the stack excluding the hidden placeholder screen that always
     * appears last in the stack. As with navStackCount, this count can include hidden screens.
     */
    val totalScreensDisplayed: Int
        get() {
            return navStack.size - 1
        }

    /**
     * Used to notify the screen factory that it needs to recompose the screens. The value returned is a random
     * number between 0 and one million. Using a randomly generated value forces LiveData to update and notify any
     * observers.
     */
    val onScreenChange: LiveData<Int> = _onScreenChange

    /**
     * An observer that clients can register with to get notified whenever the screen changes.
     *
     * @param callback When a screen change occurs, the [composable instance][ComposableInstance] of
     * the current screen will be sent to the subscriber.
     */
    fun observeScreenChange(callback: (composableInstance: ComposableInstance) -> Unit) {
        screenChangeObservers.add(callback)
    }

    /**
     * Navigates to a new screen.
     *
     * @param composableInstanceId If specified, a check will be made to see if a [composable instance][ComposableInstance]
     * exists in the navigation cache with this id. If it does exist, a new screen is added to the stack but uses
     * the cached composable instance instead of creating a new one. Make sure that this id is unique
     * throughout your app. If this parameter is null, a new id will be created for the new screen.
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
     *
     * @param cacheComposable If set to true, the composable instance will also be placed
     * permanently into a cache and remain there until explicitly removed by calling
     * [removeFromCache].
     */
    fun goto(
        composableInstanceId: String? = null,
        composableResId: String,
        p: Any? = null,
        cacheComposable: Boolean = false
    ) {
        goto(
            composableInstanceId = composableInstanceId,
            composableResId = composableResId,
            p = p,
            deepLink = null,
            cacheComposable = cacheComposable
        )
    }

    internal fun goto(
        composableInstanceId: String? = null,
        composableResId: String,
        p: Any? = null,
        deepLink: DeepLink? = null,
        cacheComposable: Boolean = false
    ) {
        var composableInstance: ComposableInstance? = null

        // Remove any screens from the stack that may have been previously terminated but are still on the stack.
        // This can happen if the gotoHomeScreen is called with cleanupScreensOnReturningHome set to false, which
        // results in the last screen still on the stack because it needs to be animated when the ScreenFactory is
        // called.

        for (i in navStack.lastIndex - 1 downTo 1) {
            if (navStack[i].isTerminated) {
                navStack.removeAt(i)
            }
        }

        // If the composable instance is provided, check if it's in the cache and use that.
        if (composableInstanceId != null) {
            composableInstance = navCache.firstOrNull { it.id == composableInstanceId }
        }

        if (composableInstance == null) {
            composableInstance = crm.createRootComposableInstance(
                composableInstanceId = composableInstanceId,
                composableResId = composableResId,
                p = p,
                deepLink = deepLink
            )

            if (cacheComposable) {
                navCache.add(composableInstance)
            }
        }

        composableInstance._onCloseScreen?.value = false
        trash.clear()
        navStack.add(navStack.lastIndex, composableInstance)
        notifyScreenChangeWithLiveDataAndCallbacks(composableInstance)
    }

    /**
     * Navigates to the home screen.
     *
     * All screens are removed from the navigation stack except the home screen (and the hidden placeholder screen) added after it.
     * Using the Back button at this point normally should cause the app to exit.
     *
     * Before navigating to the home screen, a call to
     * [onNavigateBack][dev.wirespec.jetmagic.navigation.NavigationManagerHelper.onNavigateBack] on the current screen will
     * be made if the current screen implements this interface function. The value returned by onNavigateBack will
     * determine whether the navigation to the home screen will be canceled or whether to proceed and cache the
     * current screen or remove it from the cache (if it was previously stored in the cache).
     *
     * @param cleanupScreensOnReturningHome If set to false, none of the previous screens that are currently on the
     * navigation stack will get recomposed. Only the current screen will be recomposed. This means that none of the
     * previous screens will have a chance to perform any cleanup work. Returning home as quickly as possible without
     * recomposing any of the previous screens results in a better response. If this parameter is set to true, then
     * every previous screen will get recomposed and if there are a lot of screens with a lot of cleanup work that each
     * needs to perform, the user might see an obvious delay until they are back to the home screen. If none of your
     * screens need to do any cleanup, or if you can ensure that all the screens currently on the navigation stack
     * (except the current screen) don't require any cleanup, consider setting this parameter to false for a better
     * UI response.
     *
     * @param p Any parameter data that needs to be passed to the home screen.
     */
    fun gotoHomeScreen(cleanupScreensOnReturningHome: Boolean = true, p: Any? = null) {
        clearDeepLinks()
        notifyCRMOnNavigationBackOrToHomeScreen()

        handleNavigateBackOptions {
            gotoHomeScreenImmediately(cleanupScreensOnReturningHome = cleanupScreensOnReturningHome, p = p)
        }
    }

    /**
     * Navigates to the home screen immediately.
     *
     * No check is made to see if the current screen implements
     * [onNavigateBack][dev.wirespec.jetmagic.navigation.NavigationManagerHelper.onNavigateBack].
     *
     * @param cleanupScreensOnReturningHome See the documentation for the cleanupScreensOnReturningHome parameter of
     * [gotoHomeScreen].
     *
     * @param p Any parameter data that needs to be passed to the home screen.
     */
    fun gotoHomeScreenImmediately(cleanupScreensOnReturningHome: Boolean = true, p: Any? = null): Boolean {

        clearDeepLinks()
        notifyCRMOnNavigationBackOrToHomeScreen()

        trash.clear()
        val lastScreenIndex = navStack.lastIndex - 1

        for (i in lastScreenIndex downTo 1) {
            val composableInstance = navStack[i]
            composableInstance.isTerminated = true
            trash.add(navStack[i])
            navStack.removeAt(i)

            // Notify all the children on the screen that the screen is closing.
            composableInstance.composables.forEach {
                it.isTerminated = true

                if (cleanupScreensOnReturningHome || (i == lastScreenIndex)) {
                    it._onCloseScreen?.value = true
                }
            }

            if (cleanupScreensOnReturningHome || (i == lastScreenIndex)) {
                // Notify the root composable that the screen is closing.
                composableInstance._onCloseScreen?.value = true
            }
        }

        // Store the optional parameters in the home screen.
        if (p != null) {
            navStack[0].parameters = p
            navStack[0]._onUpdate?.value = (0..MaxRandomNumber).random()
        }

        // The screen factory will not get updated when notifyScreenChangeWithCallback is called.
        notifyScreenChangeWithCallback(navStack[navStack.lastIndex - 1])

        return true
    }

    /**
     * Navigates back to the previous screen.
     *
     * If the current screen's root [composable instance][ComposableInstance]
     * implements [onNavigateBack][dev.wirespec.jetmagic.navigation.NavigationManagerHelper.onNavigateBack] in its viewmodel
     * (and the viewmodel is part of the composable instance), a call will be made to it. If onNavigateBack returns
     * [GoBackImmediately], [GoBackImmediatelyAndCacheScreen] or [GoBackImmediatelyAndRemoveCachedScreen],
     * the navigation manager will navigate to the previous screen (if one exists) If navigateBackImmediately returns
     * [Cancel], no navigation is made to the previous screen. If the current screen needs to perform clean up work or
     * prompt the user about something prior to navigating to the previous screen, the current screen can then call
     * the goBackImmediately function when it is ready to return to the previous screen.
     *
     * Once navigation to the previous screen is allowed, the current screen is removed from the navigation stack.
     * If the current screen is the home screen, then hitting the Back button should cause the app to exit.
     *
     * @return Returns true if the Navigation Manager can navigate to a previous screen. It will return false if
     * the current screen is the Home screen, meaning that there are no more screens that it could go back to.
     */
    fun goBack(): Boolean {
        clearDeepLinks()
        return handleNavigateBackOptions {
            goBackImmediately()
        }
    }

    /**
     * Navigates back to the previous screen immediately.
     *
     * No check is made to see if the current screen's [composable instance][ComposableInstance]
     * implements [onNavigateBack][dev.wirespec.jetmagic.navigation.NavigationManagerHelper.onNavigateBack] in its viewmodel
     * The current screen is removed from the navigation stack. If the current screen is the home screen, then hitting
     * the Back button should cause the app to exit.
     */
    fun goBackImmediately(): Boolean {
        notifyCRMOnNavigationBackOrToHomeScreen()

        trash.clear()

        if (navStack.size == 2) {
            navStack.removeFirst()
            this.activity?.finish()
            this.activity = null
            return false
        }

        var lastVisibleScreenRemoved = false

        while (true) {
            val lastIndex = navStack.lastIndex - 1

            if (lastIndex < 0) {
                break
            }

            val composableInstance = navStack[lastIndex]

            if (((composableInstance.deepLink == null) || !composableInstance.deepLink!!.removeScreenFromNavigationStack) && lastVisibleScreenRemoved) {
                break
            }

            val inCache = navCache.any { it.id == composableInstance.id }

            if (!inCache) {
                composableInstance.isTerminated = true
            }

            trash.add(navStack[lastIndex])

            navStack.removeAt(lastIndex)
            lastVisibleScreenRemoved = true

            // Notify all the children on the screen that the screen is closing.
            composableInstance.composables.forEach {
                if (!inCache) {
                    it.isTerminated = true
                }

                it._onCloseScreen?.value = true
            }

            // Notify the root composable that the screen is closing.
            composableInstance._onCloseScreen?.value = true
        }

        if (navStack.size > 1) {
            notifyScreenChangeWithCallback(navStack[navStack.lastIndex - 1])
            return true
        } else {
            return false
        }
    }

    /**
     * Returns a root [composable instance][ComposableInstance] for the specified index.
     *
     * @param index The first item in the navigation stack starts with zero. Use the totalScreensDisplayed
     * property to get the last index.
     */
    fun getRootComposableByIndex(index: Int): ComposableInstance {
        return navStack[index]
    }

    /**
     * Returns a root [composable instance][ComposableInstance] for the specified id.
     *
     * @param id The id of the composable instance.
     */
    fun getRootComposableInstanceById(id: String): ComposableInstance {
        return findRootComposable(id = id, returnParent = true)
    }

    /**
     * Returns a [composable instance][ComposableInstance] for the specified id.
     *
     * @param id The id of the composable instance.
     */
    fun getComposableInstanceById(id: String): ComposableInstance {
        return findRootComposable(id = id)
    }

    /**
     * Removes all items from the navigation cache.
     */
    fun clearScreenCache() {
        navCache.clear()
    }

    /**
     * Removes a composable instance from the navigation cache.
     *
     * @param composableId The id of the [composable instance][ComposableInstance] to remove.
     */
    fun removeFromCache(composableId: String) {
        val index = navCache.indexOfFirst { it.id == composableId }

        if (index >= 0) {
            navCache.removeAt(index)
        }
    }

    /**
     * Returns the current deep link if it is associated with the specified composable instance.
     *
     * If multiple screens are setup to be launched in succession when a deep link is triggered, each
     * of the screens will have a deep link associated with it. These deep links are stored in a deep
     * link stack (a collection) with the first item in the stack being the first screen that will be
     * displayed and the last screen displayed is the last item in the stack.
     *
     * Whenever a deep link results in a screen navigation, the deepLink property of a composable instance
     * will be set and it generally will remain set as long as the composable instance exists. The
     * Navigation Manager however keeps its own copy of the deep link on stack and remove the deep link
     * when a screen moves to the next deep link. Because a screen's composable instance can be recomposed
     * the deep link associated with the composable instance will not exist as soon as the Navigation Manager
     * navigates to the next deep link. But the screen where the deep link has been removed from may still need
     * to know that a deep link was used to launch the screen in order to put the screen into the appropriate
     * state. For this reason, the composable instance should not rely on the deep link returned by
     * [getDeepLinkForComposableInstance] to determine the state of a deep link. After retrieving the deep link
     * when calling [getDeepLinkForComposableInstance], the composable instance should act upon it and treat
     * it as a one-time execution. Thereafter, if the composable instance still needs to have access to the deep link,
     * it should reference the copy in its own property.
     *
     * A composable instance should not depend on its own deepLink property to know whether to navigate to the next
     * deep link. It must rely upon the value returned by calling [getDeepLinkForComposableInstance]. If the value
     * returns null, it means that there is no deep link to navigate to.
     *
     * Whenever a call to getDeepLinkForComposableInstance returns a non-nullable value, it means that its screen
     * was created as a result of a deep link being triggered.
     *
     * @param composableInstance If the first deep link on the stack is associated with the specified
     * composable instance, the deep link is returned, otherwise null is returned. If the
     * composable instance is a child of a root composable instance and if the parent is associated
     * with the first item on the deep link stack, the deep link is returned for the parent. In other
     * words, you can pass in either a root composable instance or a child composable instance and
     * as long as either is associated with the current deep link, it will be returned.
     *
     * @return If the specified composable instance is associated with the current deep link, the
     * current deep link will be returned, otherwise null will be returned.
     *
     */
    fun getDeepLinkForComposableInstance(composableInstance: ComposableInstance): DeepLink? {
        val composableInstanceId = if (composableInstance.isRoot) composableInstance.id else composableInstance.parentId

        if (deepLinks.isEmpty() || (deepLinks[0].composableInstanceId != composableInstanceId)) {
            return null
        }

        val deepLink = deepLinks[0]

        return deepLink
    }

    /**
     * Clears the stack of deep links.
     */
    fun clearDeepLinks() {
        deepLinks.clear()
    }

    /**
     * Adds a deep link to the deep link stack.
     *
     * @param map One or more deep link configurations. Each configuration can have its own list of paths that will
     * trigger a deep link.
     *
     * @param onMatchNotFound If a deep link is triggered but the path for the deep link cannot be found in any of
     * the configurations, the onMatchNotFound callback will be called. The callback can return zero or more
     * composable resource Ids for screens that will be displayed in succession. This can be used to display a
     * screen similar to a "page not found" or take the user to an alternative screen.
     *
     * The callback will be provided with the uri and query string key/values that triggered the deep link.
     */
    fun addDeepLinks(map: List<DeepLinkMap>, onMatchNotFound: (uri: URI, queryKeyValues: Map<String, String>) -> List<String>?) {
        deepLinkMap.addAll(map)
        onDeepLinkMatchNotFound = onMatchNotFound
    }

    /**
     * Causes one or more screens to be launched for the specified url.
     *
     * @param url The url that will launch the screens.
     */
    fun gotoDeepLink(url: String) {

        val uri = URI(url)
        val queryKeyValues = mutableMapOf<String, String>()

        if (!uri.query.isNullOrEmpty()) {
            val keyValuePairs = uri.query.split("&")

            keyValuePairs.forEach {
                val keyValue = it.split("=")
                queryKeyValues.put(keyValue[0], keyValue[1])
            }
        }

        val deepLinkMapFound = deepLinkMap.firstOrNull { deepLink ->
            deepLink.paths.any { pathToMatch ->
                pathsMatch(uri.path, pathToMatch)
            }
        }

        var resIds: List<String>? = null

        if (deepLinkMapFound != null) {
            resIds = deepLinkMapFound.onRequestForComposableResourceIds.invoke(uri, queryKeyValues)
        } else if (onDeepLinkMatchNotFound != null) {
            resIds = onDeepLinkMatchNotFound?.invoke(uri, queryKeyValues)
        }

        resIds?.forEach { resId ->
            deepLinks.add(
                DeepLink(
                    composableInstanceId = createId(),
                    composableResourceId = resId,
                    url = url,
                    path = uri.path,
                    queryKeyValues = queryKeyValues
                )
            )
        }

        if (deepLinks.isNotEmpty()) {
            // If there is more than one deep link, add the composable resource id
            // of the next deep link to each deep link.

            for (i in 1..deepLinks.lastIndex) {
                deepLinks[i - 1].navigateToComposableResourceId = deepLinks[i].composableResourceId
            }

            // If only the last screen is to be shown, mark all the other screens preceding
            // the last screen to be removed from the navigation stack.

            if ((deepLinkMapFound != null) && deepLinkMapFound.displayLastScreenOnly && (deepLinks.size > 1)) {
                for (i in 0 until deepLinks.lastIndex) {
                    deepLinks[i].removeScreenFromNavigationStack = true
                }
            }

            val deepLink = deepLinks[0]

            goto(
                composableInstanceId = deepLink.composableInstanceId,
                composableResId = deepLink.composableResourceId,
                deepLink = deepLink
            )
        }
    }

    /**
     * Causes the Navigation Manager to navigate to the next screen in the list of deep links.
     *
     * @param composableInstance The composable instance refers to the composable instance associated
     * with the current deep link and not the deep link of the next screen in the deep link stack. A
     * test is done to ensure that the specified composable instance is associated with the current
     * deep link and only when it is will the navigation to the next deep link begin. This is done
     * to ensure that an incorrect composable instance hasn't been passed to this function. The
     * specified composable instance has no relationship with the next deep link in the stack.
     *
     * @param p Any parameter data that needs to be passed to the next screen in the deep link stack.
     *
     */
    fun gotoNextDeepLink(composableInstance: ComposableInstance, p: Any? = null) {
        val composableInstanceId = if (composableInstance.isRoot) composableInstance.id else composableInstance.parentId

        if (deepLinks.isEmpty() || (deepLinks[0].composableInstanceId != composableInstanceId)) {
            return
        }

        deepLinks.removeFirst()

        if (deepLinks.isEmpty()) {
            return
        }

        val deepLink = deepLinks[0]

        goto(
            composableInstanceId = deepLink.composableInstanceId,
            composableResId = deepLink.composableResourceId,
            p = p,
            deepLink = deepLink
        )
    }

    private fun createId(): String {
        return (0..1_000_000).random().toString()
    }

    private fun pathsMatch(srcPath: String, matchTo: String): Boolean {
        if (matchTo.startsWith("[%") && matchTo.endsWith("%]")) {
            val pattern = matchTo.substring(2, matchTo.lastIndex - 1).trim()
            val regex = pattern.toRegex(RegexOption.IGNORE_CASE)
            return regex.matches(srcPath)

        } else {
            return srcPath == matchTo
        }
    }

    private fun notifyCRMOnNavigationBackOrToHomeScreen() {
        // Get the current screen.
        val composableInstance = navStack[navStack.lastIndex - 1]
        crm.onNavigationBackOrToHomeScreen(composableInstance = composableInstance)
    }

    private fun findRootComposable(id: String, returnParent: Boolean = false): ComposableInstance {
        for (i in 0..navStack.lastIndex) {
            val composableInstance = navStack[i]

            if (composableInstance.id == id) {
                return composableInstance
            }

            val c = composableInstance.composables.firstOrNull { it.id == id }

            if (c != null) {
                if (returnParent) {
                    return composableInstance
                }

                return c
            }
        }

        val composableInstance = trash.firstOrNull { it.id == id }

        if (composableInstance != null) {
            return composableInstance
        }

        throw Exception("No composable exists for id: $id")
    }

    private fun notifyScreenChangeWithLiveDataAndCallbacks(composableInstance: ComposableInstance) {
        _onScreenChange.value = (0..MaxRandomNumber).random()
        notifyScreenChangeWithCallback(composableInstance)
    }

    private fun notifyScreenChangeWithCallback(composableInstance: ComposableInstance) {
        val iterator = screenChangeObservers.iterator()

        while (iterator.hasNext()) {
            val observer = iterator.next()

            try {
                observer(composableInstance)
            } catch (e: Exception) {
                iterator.remove()
            }
        }
    }

    private fun handleNavigateBackOptions(gotoHandler: () -> Boolean): Boolean {
        val currentScreenRootComposable = navStack[navStack.lastIndex - 1]
        var viewmodel: ViewModel? = currentScreenRootComposable.viewmodel

        if ((viewmodel == null) || (viewmodel !is NavigationManagerHelper)) {
            viewmodel = currentScreenRootComposable.composables.firstOrNull { it.viewmodel != null }?.viewmodel
        }

        if ((viewmodel != null) && (viewmodel is NavigationManagerHelper)) {
            val navHelper = viewmodel as NavigationManagerHelper

            when (navHelper.onNavigateBack()) {
                Cancel, null -> {
                    return true
                }
                GoBackImmediately -> {
                    return gotoHandler()
                }
                GoBackImmediatelyAndCacheScreen -> {
                    if (!navCache.any { it.id == currentScreenRootComposable.id }) {
                        navCache.add(currentScreenRootComposable)
                    }
                }
                GoBackImmediatelyAndRemoveCachedScreen -> {
                    val index = navCache.indexOfFirst { it.id == currentScreenRootComposable.id }

                    if (index >= 0) {
                        navCache.removeAt(index)
                    }
                }
            }
        }

        return gotoHandler()
    }
}

@SuppressLint("StaticFieldLeak")
lateinit var navman: NavigationManager