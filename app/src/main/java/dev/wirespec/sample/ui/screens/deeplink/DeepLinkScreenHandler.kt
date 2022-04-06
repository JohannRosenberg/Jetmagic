package dev.wirespec.sample.ui.screens.deeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.sample.ui.ComposableResourceIDs

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

