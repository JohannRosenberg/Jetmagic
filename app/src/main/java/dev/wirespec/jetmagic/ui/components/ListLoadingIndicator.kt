package dev.wirespec.jetmagic.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.R
import dev.wirespec.jetmagic.ui.theme.MaterialColors

/**
 * Can be used to display a loading indicator when a list is being loaded.
 */
@Composable
fun ListLoadingIndicator(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(top = 10.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(stringResource(R.string.loading))
            }
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(bottom = 20.dp), horizontalArrangement = Arrangement.Center
            ) {
                LinearProgressIndicator(modifier = modifier.requiredWidth(110.dp), color = MaterialColors.cyan500)
            }
        }
    }
}