package dev.wirespec.jetmagic.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun MultiLineText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colors.primary
) {
    val lines = text.split("\\n")

    if (lines.size == 1) {
        Text(lines[0], modifier = modifier, color = color)
    } else {
        modifier.padding(bottom = 3.dp)

        for (i in 0 until lines.size - 1) {
            Text(lines[i], modifier = modifier, color = color)
        }

        Text(lines[lines.size - 1], modifier = modifier, color = color)
    }
}
