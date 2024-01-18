package io.github.johannrosenberg.sample.ui.screens.petslist.xlarge.land

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.sample.models.PetDetailsParams
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs

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


