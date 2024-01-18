package io.github.johannrosenberg.jetmagic.models

import io.github.johannrosenberg.jetmagic.navigation.NavigationManager

/**
 * Provides information about a deep link to composable instances when the app is activated by a deep link.
 */
data class DeepLink(
    /**
     * The id of the composable instance that the deep link is associated with.
     */
    val composableInstanceId: String,

    /**
     * The id of the composable resource that was used to render the composable instance.
     */
    val composableResourceId: String,

    /**
     * The url (or uri) that was used to launch the deep link.
     */
    val url: String,

    /**
     * The path of the url that was used to launch the deep link. This excludes the protocol and domain. For example, if
     * the url that was used to launch the deep link was https://github.com/johannrosenberg/jetmagic/sample/get_pet?id=123,
     * the path will be /jetmagic/sample/get_pet without the protocol, domain or query string.
     */
    val path: String,

    /**
     * A collection of key/values where the key is the name of the query string parameter and the value is the value
     * the parameter is set to.
     */
    val queryKeyValues: Map<String, String>,

    /**
     * If set, this indicates the composable resource that will be used to navigate to the next screen in the
     * deep link. The composable resource ids are defined when [addDeepLinks][NavigationManager.addDeepLinks] is
     * called to setup the deep links.
     */
    var navigateToComposableResourceId: String? = null,

    /**
     * If set to true, the composable instance should prevent its UI from being rendered. This allows a deep link to
     * launch multiple screens but only display the last screen. The other screens can be used to pass data to each
     * following screen if it needs to.
     */
    var removeScreenFromNavigationStack: Boolean = false
)