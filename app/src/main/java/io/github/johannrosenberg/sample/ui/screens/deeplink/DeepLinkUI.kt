package io.github.johannrosenberg.sample.ui.screens.deeplink

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
import io.github.johannrosenberg.jetmagic.models.ComposableInstance
import io.github.johannrosenberg.jetmagic.models.LocalComposableInstance
import io.github.johannrosenberg.jetmagic.navigation.navman

@Composable
fun DeepLinkHandler(composableInstance: ComposableInstance) {

    val parentComposableInstance = LocalComposableInstance.current

    var text = composableInstance.parameters as String?
    var nextScreenText = ""

    if (text == null) {
        text = "...and bears oh my!"
        nextScreenText = "...tigers"
    } else if (text  == "...tigers") {
        nextScreenText = "Lions..."
    }

    val deepLink = navman.getDeepLinkForComposableInstance(composableInstance = composableInstance)

    if (deepLink != null) {
        navman.gotoNextDeepLink(composableInstance = composableInstance, p = nextScreenText)
    }

    if (parentComposableInstance.deepLink?.removeScreenFromNavigationStack == true) {
        return
    }

    DeepLink(text = text)
}

@Composable
fun DeepLink(
    modifier: Modifier = Modifier,
    text: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxSize()
            // Important: verticalScroll must be before padding to prevent click events along the padded edge
            // from propagating to the screen below.
            .verticalScroll(rememberScrollState())
            .background(MaterialTheme.colorScheme.surface)
            .padding(10.dp)
    ) {
        Text(text = text)
    }
}


