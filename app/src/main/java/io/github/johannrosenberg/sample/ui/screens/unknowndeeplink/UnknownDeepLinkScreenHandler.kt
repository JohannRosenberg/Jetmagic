package io.github.johannrosenberg.sample.ui.screens.unknowndeeplink

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs

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

