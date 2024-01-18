package io.github.johannrosenberg.sample.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs
import io.github.johannrosenberg.sample.ui.screens.main.MainHandler
import io.github.johannrosenberg.sample.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        navman.activity = this

        if (intent.dataString != null) {
            navman.gotoDeepLink(url = intent.dataString!!)
        } else {
            if (navman.totalScreensDisplayed == 0) {
                navman.goto(composableResId = ComposableResourceIDs.PetsListScreen)
            }
        }

        setContent {
            AppTheme {
                MainHandler()
            }
        }
    }

    override fun onBackPressed() {
        if (!navman.goBack())
            super.onBackPressed()
    }

    override fun onDestroy() {
        crm.onConfigurationChanged()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent?.dataString != null) {
            navman.gotoDeepLink(url = intent.dataString!!)
        }
    }
}
