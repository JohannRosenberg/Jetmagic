package dev.wirespec.jetmagic.ui.screens.petdetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@Composable
fun PetDetailsScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderChildComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.PetDetails,
            childComposableId = "petDetails",
            p = composableInstance.parameters
        )
    }
}

