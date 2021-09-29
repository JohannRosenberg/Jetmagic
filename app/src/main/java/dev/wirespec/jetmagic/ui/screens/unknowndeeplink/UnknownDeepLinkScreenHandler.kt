package dev.wirespec.jetmagic.ui.screens.unknowndeeplink

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@ExperimentalMaterialApi
@Composable
fun UnknownDeepLinkScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.UnknownDeepLink,
            childComposableId = "unknownDeepLink",
            p = composableInstance.parameters
        )
    }
}

