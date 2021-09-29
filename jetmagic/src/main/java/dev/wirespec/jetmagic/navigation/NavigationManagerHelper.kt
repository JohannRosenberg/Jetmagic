package dev.wirespec.jetmagic.navigation

import dev.wirespec.jetmagic.models.ComposableInstance

interface NavigationManagerHelper {
    /**
     * A callback that gets called when the Navigation Manager is about to navigate either back to
     * the previous screen or navigate directly to the home screen. The value returned determines
     * how the Navigation Manager will handle the navigation. Return null to have the retu
     */
    fun onNavigateBack(): NavigateBackOptions?
}

enum class NavigateBackOptions {
    /**
     * Causes the Navigation Manager to navigate to the previous screen without checking to see if the onNavigateBack
     * interface function is defined.
     */
    GoBackImmediately,

    /**
     * Same as GoBackImmediately but the navigation info for the current screen is cached so that it can be reused
     * later on.
     */
    GoBackImmediatelyAndCacheScreen,

    /**
     * Returns to the previous screen but the [composable instance][ComposableInstance] of the current screen is
     * removed from the navigation cache if it exists in the cache.
     */
    GoBackImmediatelyAndRemoveCachedScreen,

    /**
     * Cancels navigating to the previous screen.
     */
    Cancel
}