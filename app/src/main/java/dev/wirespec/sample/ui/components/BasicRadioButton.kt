package dev.wirespec.sample.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.RadioButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = {
                onClick(id, text)
            }
        )

        Text(text = text,)
    }
}