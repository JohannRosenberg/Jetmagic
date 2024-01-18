package io.github.johannrosenberg.sample.ui.screens.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs

@Composable
fun DeepLinkScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderChildComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.DeepLink,
            childComposableId = "deepLink",
            p = composableInstance.parameters
        )
    }
}

