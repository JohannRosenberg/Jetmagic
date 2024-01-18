package io.github.johannrosenberg.jetmagic.models

import androidx.compose.ui.Modifier

/**
 * Used to pass data between composables.
 */
open class ComposableParams(
    var modifier: Modifier = Modifier,
    var data: Any? = null,
    var onReturn: ((data: Any?, canceled: Boolean) -> Unit)? = null
)