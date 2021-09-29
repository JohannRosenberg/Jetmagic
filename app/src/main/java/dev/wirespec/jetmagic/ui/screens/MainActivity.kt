package dev.wirespec.jetmagic.ui.screens

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.ui.ExperimentalComposeUiApi
import coil.annotation.ExperimentalCoilApi
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs
import dev.wirespec.jetmagic.ui.screens.main.MainHandler
import dev.wirespec.jetmagic.ui.theme.AppTheme.Companion.SetAppTheme
import dev.wirespec.jetmagic.ui.theme.ColorThemes

@ExperimentalCoilApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@ExperimentalComposeUiApi
@ExperimentalFoundationApi
class MainActivity : AppCompatActivity() {
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
            SetAppTheme(ColorThemes.DefaultLight) {
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
