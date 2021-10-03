package dev.wirespec.jetmagic.ui.screens.petslist.xlarge.land

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableInstance
import dev.wirespec.jetmagic.models.LocalComposableInstance
import dev.wirespec.jetmagic.models.PetDetailsParams
import dev.wirespec.jetmagic.ui.ComposableResourceIDs

@Composable
fun PetsListWithDetailsScreenHandler(composableInstance: ComposableInstance) {

    CompositionLocalProvider(LocalComposableInstance provides composableInstance) {
        Row(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                crm.RenderChildComposable(
                    parentComposableId = composableInstance.id,
                    composableResId = ComposableResourceIDs.PetsList,
                    childComposableId = "petsList",
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                crm.RenderChildComposable(
                    parentComposableId = composableInstance.id,
                    composableResId = ComposableResourceIDs.PetDetails,
                    childComposableId = "petDetails",
                    p = PetDetailsParams(displayAppBar = false)
                )
            }
        }
    }
}


