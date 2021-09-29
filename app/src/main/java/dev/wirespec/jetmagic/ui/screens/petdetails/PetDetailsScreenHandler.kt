package dev.wirespec.jetmagic.ui.screens.petdetails

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@ExperimentalAnimationApi
@ExperimentalMaterialApi
@Composable
fun PetDetailsScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        crm.RenderComposable(
            parentComposableId = composableInstance.id,
            composableResId = ComposableResourceIDs.PetDetails,
            childComposableId = "petDetails",
            p = composableInstance.parameters
        )
    }
}

