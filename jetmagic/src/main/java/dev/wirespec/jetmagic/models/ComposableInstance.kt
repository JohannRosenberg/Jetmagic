package dev.wirespec.jetmagic.models

import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dev.wirespec.jetmagic.composables.ComposableResourceManager


/**
 * Represents a composable instance. A composable instance encapsulates all the properties needed
 * to render (compose/recompose) a composable based on a device's configuration. A composable instance
 * can also optionally include a viewmodel.
 */
open class ComposableInstance(
    /**
     * An id that uniquely identifies the composable instance. It can be either provided by the client or randomly generated by
     * CRM if one is not provided by the client.
     */
    var id: String,

    /**
     * If the composable is a child composable, the parent id refers to the composable that it is a child
     * of.
     */
    var parentId: String? = null,

    /**
     * This identifies the composable resource that the instance is associated with. A composable resource
     * with the same id can be provided by the client when the
     * [addComposableResources][ComposableResourceManager.addComposableResources] function is called.
     * For example, multiple composable resources with the id "PetsList" can be provided to addComposableResources
     * but where each resource has its own unique set of configuration qualifiers.
     */
    var composableResId: String,

    /**
     * Set to true if this composable is the root composable on the screen.
     */
    var isRoot: Boolean = false,

    /**
     * When a composable resource is selected to render the composable, the id for the selected
     * composable resource is used here. This id originates from [ComposableResource.id] which is
     * generated internally by the [ComposableResourceManager]
     */
    var selectedResourceId: String? = null,

    /**
     * Contains any parameters that need to be passed to the composable instance.
     */
    var parameters: Any? = null,

    /**
     * An optional viewmodel that can be assigned to the composable instance.
     */
    var viewmodel: ViewModel? = null,

    /**
     * Lists all of the children composables that the composable instance may have. Only a
     * root composable instance can have children. Although a child composable instance will
     * still have this property, it will not be used. Support for deeply nested descendant
     * composable instances is not currently supported (grandchildren, great grandchildren, etc).
     * This means that if you have root composable that contains children instance composables, those
     * children should not have any children of their own.
     */
    val composables: MutableList<ComposableInstance> = mutableListOf(),

    internal var _onUpdate: MutableLiveData<Int>? = null,

    /**
     * Used to notify the composable instance when it is being updated.
     *
     * A typical case is when a screen consists of two composable instances - one
     * a list in the left pane and a details pane on the right. When the user
     * clicks on a list item, the details pane needs to be updated. The details pane can
     * be updated if the list calls [updateOrGoto][ComposableResourceManager.updateOrGoto]. The value sent by LiveData is
     * a random number and has no meaning. It is simply used to trigger the LiveData. The
     * details screen is then responsible to retrieve any updated data through the [parameters]
     * property of its own composable instance.
     *
     * Whenever the LiveData is triggered, the composable instance performs a recompose.
     */
    var onUpdate: LiveData<Int>? = null,

    internal var _onCloseScreen: MutableLiveData<Boolean>? = null,

    /**
     * Used to notify the composable instance that the screen on which they located
     * is being closed. Composable instances should then take action to perform any
     * cleanup they need as well as prevent any processes from being carried out that
     * would normally be executed when the screen is being made visible the first time.
     *
     * Whenever the LiveData is triggered, the composable instance performs a recompose.
     */
    var onCloseScreen: LiveData<Boolean> = MutableLiveData(false), //  ? = null,

    /**
     * If set to true, the composable is terminated and no longer part of the navigation stack. A call to
     * [RenderComposable][ComposableResourceManager.RenderComposable] or
     * [RenderChildComposable][ComposableResourceManager.RenderChildComposable] can still be
     * made with the composable when a screen where
     * the composable is located is being closed. This allows the screen to be recomposed during animation
     * transitions to become invisible.
     */
    var isTerminated: Boolean = false,

    /**
     * Indicates the animation transition state when the composable instance is being shown or hidden with animation.
     */
    var animationTransitionState: MutableTransitionState<Boolean>? = null,

    /**
     * Provides information about the deep link that was used to display the composable instance.
     */
    var deepLink: DeepLink? = null
)

val LocalComposableInstance = staticCompositionLocalOf { ComposableInstance(id = "", composableResId = "") }