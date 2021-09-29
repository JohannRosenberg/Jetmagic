package dev.wirespec.jetmagic.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BasicRadioButton(
    id: String,
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: (id: String, text: String) -> Unit
) {
    Row(
        modifier
            .padding(bottom = 10.dp)
            .selectable(
                selected = selected,
                onClick = {
                    onClick(id, text)
                }
            )
            .padding(horizontal = 16.dp)
    ) {
        RadioButton(
            selected = selected,
            onClick = {
                onClick(id, text)
            }
        )
        Text(
            text = text,
            style = MaterialTheme.typography.body1.merge(),
            modifier = modifier.padding(start = 16.dp)
        )
    }
}