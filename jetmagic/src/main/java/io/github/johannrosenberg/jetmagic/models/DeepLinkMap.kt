package io.github.johannrosenberg.jetmagic.models

import java.net.URI

/**
 * Used to define a deep link.
 */
data class DeepLinkMap(
    /**
     * A collection of paths that the deep link will respond to. A path can also be a regular expression. These
     * are paths without the protocol or domain. Also, don't include any query string parameters at the end of
     * the path.
     *
     * To indicate that a path is a regular expression, it must be enclosed in [% %]. For example, this regular
     * expression:
     *
     * [%/jetmagic/sample/[4-6]/animals%]
     *
     * will match
     *
     * /jetmagic/sample/4/animals
     * /jetmagic/sample/5/animals
     * /jetmagic/sample/6/animals
     *
     * but will not match any sub path that is not 4, 5 or 6.
     */
    val paths: List<String>,

    /**
     * If set to true and multiple screens are defined by the onRequestForComposableResourceIds list,
     * only the last screen in the list will be shown. The other screens will be rendered but it is
     * the responsibility of each screen to decide whether to display its UI or not. When displayLastScreenOnly
     * is set to true, [DeepLink.removeScreenFromNavigationStack] will be set to true in the composable instance
     * for all the screens except the last screen.
     *
     * Setting displayLastScreenOnly to true is useful when you need the target screen that is displayed
     * is dependent on other screens and those other screens should be launched prior to the target screen
     * being displayed. It is also useful if the target screen doesn't want to have to obtain its data
     * when it could receive the data from a previous screen. An example is the sample app where the pet details
     * screen receives the pet details from the pets list screen. Without receiving this data, the pet details
     * screen would have to parse the deep link url and retrieve the data from the repository based on the
     * query parameters.
     *
     */
    val displayLastScreenOnly: Boolean = false,


    /**
     * A callback that gets called when a deep link has started and the composable resources are needed
     * that will be used to render the composable instances.
     *
     * @param uri The uri that was used to launch the deep link.
     *
     * @param queryKeyValues: A collection that contains the query string keys and values.
     *
     * @return A list of one or more composable resource ids. The order in which the ids are added
     * to the collection is the order in which the screens will be rendered.
     */
    val onRequestForComposableResourceIds: (uri: URI, queryKeyValues: Map<String, String>) -> List<String>?
)