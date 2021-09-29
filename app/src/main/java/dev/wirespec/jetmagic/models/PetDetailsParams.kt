package dev.wirespec.jetmagic.models

import androidx.compose.ui.Modifier

/**
 * Used to pass data to the pet details composable.
 */
class PetDetailsParams(
    modifier: Modifier = Modifier,
    data: Any? = null,
    var petsListItemInfo: PetListItemInfo? = null,
    var displayAppBar: Boolean = true
) : ComposableParams(modifier = modifier, data = data)