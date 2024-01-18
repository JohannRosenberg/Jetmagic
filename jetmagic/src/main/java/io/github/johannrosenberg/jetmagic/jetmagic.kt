package io.github.johannrosenberg.jetmagic

import android.content.Context
import io.github.johannrosenberg.jetmagic.composables.ComposableResourceManager
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.navigation.NavigationManager
import io.github.johannrosenberg.jetmagic.navigation.navman

/**
 * Initializes Jetmagic.
 *
 * This should only be used in apps that use a single activity instance. It should be called during the app's startup.
 * The recommended location to call this function is in a class that inherits from Application in the onCreate function.
 *
 * Calling this function sets the global navman and crm variables which can then be called anywhere with the app.
 *
 * If your app is not a single instance activity, you should avoid using this and instead create separate instances
 * of the NavigationManager and ComposableResourceManager for each activity.
 */
fun initializeJetmagic(context: Context) {
    navman = NavigationManager()
    crm = ComposableResourceManager()
    crm.setContext(context)
    crm.navMan = navman
    navman.crm = crm
}