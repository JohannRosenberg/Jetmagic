package dev.wirespec.sample.ui.screens.unknowndeeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.sample.ui.ComposableResourceIDs

@Composable
fun UnknownDeepLinkScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderChildComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.UnknownDeepLink,
            childComposableId = "unknownDeepLink",
            p = composableInstance.parameters
        )
    }
}

