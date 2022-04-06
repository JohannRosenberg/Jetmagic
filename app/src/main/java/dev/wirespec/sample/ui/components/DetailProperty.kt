package dev.wirespec.sample.ui.components

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun DetailProperty(
    @StringRes labelResId: Int,
    propValue: String,
    modifier: Modifier = Modifier) {
    Column(modifier =  modifier.padding(start = 10.dp, end= 10.dp, bottom = 10.dp)) {
        Text(stringResource(labelResId), style = MaterialTheme.typography.subtitle2, color = MaterialTheme.colors.secondary)
        Text(text = propValue)
    }
}