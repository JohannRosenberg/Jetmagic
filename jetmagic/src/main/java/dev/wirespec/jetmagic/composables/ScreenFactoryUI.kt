package dev.wirespec.jetmagic.composables

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.ComposableResource
import dev.wirespec.jetmagic.navigation.navman


/**
 * Composes or recomposes all of the screens on the navigation stack providing an animated transition when
 * navigating between screens. Each screen can have its own animation. By default the slide in / slide out animation
 * is used when navigating to and from screens.
 */
@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun ScreenFactoryHandler() {

    // The Navigation Manager will notify the screen factory whenever it needs to recompose all of the screens.
    navman.onScreenChange.observeAsState().value

    val lastIndex = navman.navStackCount - 1

    for (i in 0..lastIndex) {
        val composableInstance = navman.getRootComposableByIndex(i)

        ScreenFactory(
            isVisible = i < lastIndex,
            composableInstance = composableInstance
        )
    }
}

@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun ScreenFactory(
    isVisible: Boolean,
    composableInstance: ComposableInstance,
    modifier: Modifier = Modifier
) {
    // Each screen gets notified whenever it is being closed.
    var closeScreen = composableInstance.onCloseScreen?.observeAsState(false)?.value

    if (closeScreen == null)
        closeScreen = false

    lateinit var composableResource: ComposableResource
    var useCustomAnimation = false

    if (composableInstance.id.isNotEmpty()) {
        composableResource = crm.getComposableResourceForComposableInstance(composableInstance = composableInstance)
        useCustomAnimation = composableResource.onAnimateVisibility != null
    }

    val visible = isVisible && !closeScreen

    if (useCustomAnimation) {
        // NOTE: Initially, no transition state exists for a composable instance that has a custom
        // animation. After creating one, that instance is used until the screen no longer
        // remains visible.
        if (composableInstance.animationTransitionState == null) {
            setAnimationTransitionState(composableInstance = composableInstance, initialState = false)
        } else if (!visible) {
            setAnimationTransitionState(composableInstance = composableInstance, initialState = !visible)
        }

        composableResource.onAnimateVisibility?.invoke(composableInstance = composableInstance)
    } else {
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(500)),
            exit = slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(500))
        ) {
            if (composableInstance.id.isNotEmpty()) {
                crm.RenderComposable(composableInstance = composableInstance)
            } else {
                Surface(modifier = modifier.fillMaxSize()) {}
            }
        }
    }
}

private fun setAnimationTransitionState(composableInstance: ComposableInstance, initialState: Boolean) {
    composableInstance.animationTransitionState = MutableTransitionState(initialState).apply {
        targetState = !initialState
    }
}