package io.github.johannrosenberg.jetmagic

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import io.github.johannrosenberg.jetmagic.composables.ScreenOrientation
import io.github.johannrosenberg.jetmagic.composables.ScreenSize
import io.github.johannrosenberg.jetmagic.composables.crm
import io.github.johannrosenberg.jetmagic.models.ComposableResource
import io.github.johannrosenberg.jetmagic.models.DeepLinkMap
import io.github.johannrosenberg.jetmagic.navigation.navman
import io.github.johannrosenberg.sample.ui.ComposableResourceIDs
import io.github.johannrosenberg.sample.ui.screens.DeepLinkPaths
import io.github.johannrosenberg.sample.ui.screens.catselection.CatSelectionHandler
import io.github.johannrosenberg.sample.ui.screens.catselection.CatSelectionScreenHandler
import io.github.johannrosenberg.sample.ui.screens.deeplink.DeepLinkHandler
import io.github.johannrosenberg.sample.ui.screens.deeplink.DeepLinkScreenHandler
import io.github.johannrosenberg.sample.ui.screens.main.MainViewModel
import io.github.johannrosenberg.sample.ui.screens.petdetails.PetDetailsHandler
import io.github.johannrosenberg.sample.ui.screens.petdetails.PetDetailsScreenHandler
import io.github.johannrosenberg.sample.ui.screens.petdetails.PetDetailsViewModel
import io.github.johannrosenberg.sample.ui.screens.petslist.PetsListHandler
import io.github.johannrosenberg.sample.ui.screens.petslist.PetsListScreenHandler
import io.github.johannrosenberg.sample.ui.screens.petslist.PetsListViewModel
import io.github.johannrosenberg.sample.ui.screens.petslist.xlarge.land.PetsListWithDetailsScreenHandler
import io.github.johannrosenberg.sample.ui.screens.prompttogoback.PromptToGoBackHandler
import io.github.johannrosenberg.sample.ui.screens.prompttogoback.PromptToGoBackScreenHandler
import io.github.johannrosenberg.sample.ui.screens.prompttogoback.PromptToGoBackScreenViewModel
import io.github.johannrosenberg.sample.ui.screens.test.TestHandler
import io.github.johannrosenberg.sample.ui.screens.test.TestScreenHandler
import io.github.johannrosenberg.sample.ui.screens.test.TestViewModel
import io.github.johannrosenberg.sample.ui.screens.unknowndeeplink.UnknownDeepLinkHandler
import io.github.johannrosenberg.sample.ui.screens.unknowndeeplink.UnknownDeepLinkScreenHandler
import java.util.Locale


/**
 * Inherits from Application and is used for things like accessing the app's context and setting up
 * resources during the app's startup.
 */
class App : Application() {

    private val activityLifecycleTracker: AppLifecycleTracker = AppLifecycleTracker()

    override fun onCreate() {
        super.onCreate()
        context = this

        initializeJetmagic(this)

        registerActivityLifecycleCallbacks(activityLifecycleTracker)

        /**
         * Define all the screens and those composables used as the root composables
         * on screens. The order in which the composable resources are added is not
         * important, but visually it is useful if the screen resources are listed
         * first followed by those composable resources that are used on those
         * screens.
         */

        crm.apply {
            addComposableResources(
                mutableListOf(
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetsListScreen,
                    ) { composableInstance ->
                        // PetsList default screen.
                        PetsListScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetsListScreen,
                        screenOrientation = ScreenOrientation.Landscape,
                        screenSize = ScreenSize.XLarge,
                    ) { composableInstance ->
                        // PetDetails screen in landscape mode on a tablet.
                        PetsListWithDetailsScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetDetailsScreen,
                    ) { composableInstance ->
                        // PetDetails default screen.
                        PetDetailsScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.CatSelectionScreen,
                    ) { composableInstance ->
                        // CatSelection default screen.
                        CatSelectionScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.TestScreen,
                        onAnimateVisibility = { composableInstance ->
                            AnimatedVisibility(
                                visibleState = composableInstance.animationTransitionState!!,
                                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(800)),
                                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(800)),
                            ) {
                                RenderComposable(composableInstance = composableInstance)
                            }
                        }
                    ) { composableInstance ->
                        // Test default screen.
                        TestScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.UnknownDeepLinkScreen,
                        onAnimateVisibility = { composableInstance ->
                            AnimatedVisibility(
                                visibleState = composableInstance.animationTransitionState!!,
                                enter = fadeIn(
                                    initialAlpha = 0f,
                                    animationSpec = tween(durationMillis = 900)
                                ),
                                exit = fadeOut(
                                    animationSpec = tween(durationMillis = 900)
                                )
                            ) {
                                RenderComposable(composableInstance = composableInstance)
                            }
                        }
                    ) { composableInstance ->
                        // Unknown deep link default screen.
                        UnknownDeepLinkScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.DeepLinkScreen1,
                    ) { composableInstance ->
                        // Deep link screen 1.
                        DeepLinkScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.DeepLinkScreen2,
                    ) { composableInstance ->
                        // Deep link screen 2.
                        DeepLinkScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.DeepLinkScreen3,
                    ) { composableInstance ->
                        // Deep link screen 3.
                        DeepLinkScreenHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PromptToGoBackScreen,
                        viewmodelClass = PromptToGoBackScreenViewModel::class.java
                    ) { composableInstance ->
                        // Prompt to return to previous screen
                        PromptToGoBackScreenHandler(composableInstance)
                    },

                    // ************** List all children resources. **************

                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetsList,
                        viewmodelClass = PetsListViewModel::class.java
                    ) { composableInstance ->
                        // PetsList default
                        PetsListHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetDetails,
                        viewmodelClass = PetDetailsViewModel::class.java
                    ) { composableInstance ->
                        // PetDetails default.
                        PetDetailsHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetDetails,
                        viewmodelClass = PetDetailsViewModel::class.java,
                        screenOrientation = ScreenOrientation.Landscape
                    ) { composableInstance ->
                        // PetDetails in landscape mode.
                        io.github.johannrosenberg.sample.ui.screens.petdetails.land.PetDetailsHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetDetails,
                        viewmodelClass = PetDetailsViewModel::class.java,
                        languageAndRegion = "de",
                    ) { composableInstance ->
                        // PetDetails in German.
                        io.github.johannrosenberg.sample.ui.screens.petdetails.de.PetDetailsHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.Test,
                        viewmodelClass = TestViewModel::class.java
                    ) { composableInstance ->
                        // Test default.
                        TestHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.CatSelection,
                    ) { composableInstance ->
                        // Cat selection default.
                        CatSelectionHandler(composableInstance)
                    },
                    ComposableResource(
                        // Unknown deep link default.
                        resourceId = ComposableResourceIDs.UnknownDeepLink,
                    ) { composableInstance ->
                        // Unknown deep link default.
                        UnknownDeepLinkHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.DeepLink,
                    ) { composableInstance ->
                        // Deep link default.
                        DeepLinkHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PromptToGoBack
                    ) { composableInstance ->
                        // Prompt to return to previous screen default.
                        PromptToGoBackHandler(composableInstance)
                    },
                )
            )
        }

        // Add deep links.
        navman.addDeepLinks(
            map = mutableListOf(
                DeepLinkMap(paths = mutableListOf(DeepLinkPaths.root)) { uri, queryKeyValues ->
                    mutableListOf(ComposableResourceIDs.PetsListScreen)
                },
                DeepLinkMap(paths = mutableListOf(DeepLinkPaths.petInfo)) { uri, queryKeyValues ->
                    mutableListOf(ComposableResourceIDs.PetDetailsScreen)
                },
                DeepLinkMap(paths = mutableListOf(DeepLinkPaths.deepLink, DeepLinkPaths.deepLinkWithRegex), displayLastScreenOnly = false) { uri, queryKeyValues ->
                    mutableListOf(ComposableResourceIDs.DeepLinkScreen1, ComposableResourceIDs.DeepLinkScreen2, ComposableResourceIDs.DeepLinkScreen3)
                },
            )
        ) { uri, queryKeyValues ->
            mutableListOf(ComposableResourceIDs.UnknownDeepLinkScreen)
        }
    }

    companion object {
        lateinit var context: App
        lateinit var mainViewModel: MainViewModel
        var appLocale = "en"
    }

    // Returns the current activity.
    var currentActivity: Activity?
        get() = activityLifecycleTracker.currentActivity
        private set(value) {}

    fun setAppLocale(language: String) {
        appLocale = language

        // IMPORTANT: Always call applyConfigChangesNow after making any config changes
        // programmatically but before restarting the activity.
        // NOTE: The language change doesn't actually take effect until
        // the activity has been restarted. The app's language is
        // set in onActivityResumed.

        crm.applyConfigChangesNow()
        val act = context.currentActivity as Activity
        act.startActivity(Intent.makeRestartActivityTask(act.componentName))
    }

    /**
     * Callbacks for handling the lifecycle of activities.
     */
    class AppLifecycleTracker : ActivityLifecycleCallbacks {

        private var currentAct: Activity? = null

        var currentActivity: Activity?
            get() = currentAct
            private set(value) {}

        override fun onActivityCreated(activity: Activity, p1: Bundle?) {
        }

        override fun onActivityStarted(activity: Activity) {
        }

        override fun onActivityResumed(activity: Activity) {
            currentAct = activity
            context.resources.configuration.setLocale(Locale(appLocale))
            context.resources.updateConfiguration(context.resources.configuration, context.resources.displayMetrics)
        }

        override fun onActivityPaused(p0: Activity) {
        }

        override fun onActivityStopped(activity: Activity) {
            if ((currentAct != null) && (activity == currentAct))
                currentAct = null
        }

        override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
        }

        override fun onActivityDestroyed(p0: Activity) {
        }
    }
}

