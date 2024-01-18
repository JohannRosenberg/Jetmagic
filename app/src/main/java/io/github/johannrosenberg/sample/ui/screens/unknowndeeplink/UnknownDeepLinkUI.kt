package io.github.johannrosenberg.sample.ui.screens.unknowndeeplink

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.ui.theme.MaterialColors

@Composable
fun UnknownDeepLinkHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    val url = if (parentComposableInstance.deepLink != null) {
        parentComposableInstance.deepLink!!.url
    } else {
        ""
    }

    val deepLink = navman.getDeepLinkForComposableInstance(composableInstance = composableInstance)

    if (deepLink != null) {
        // Since we don't support any further deep links beyond the details screen,
        // clear any additional deep links that might still be on the stack.
        navman.clearDeepLinks()
    }

    UnknownDeepLink(url = url)
}

@Composable
fun UnknownDeepLink(
    url: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(text = "No screen mapped to the url:", fontSize = 16.sp, modifier = Modifier.padding(bottom = 10.dp))
        Text(text = url, fontSize = 18.sp, color = MaterialColors.red500)
    }
}


