package dev.wirespec.jetmagic

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import dev.wirespec.jetmagic.composables.ScreenOrientation
import dev.wirespec.jetmagic.composables.ScreenSize
import dev.wirespec.jetmagic.composables.crm
import dev.wirespec.jetmagic.models.ComposableResource
import dev.wirespec.jetmagic.models.DeepLinkMap
import dev.wirespec.jetmagic.navigation.navman
import dev.wirespec.jetmagic.ui.ComposableResourceIDs
import dev.wirespec.jetmagic.ui.screens.DeepLinkPaths
import dev.wirespec.jetmagic.ui.screens.catselection.CatSelectionHandler
import dev.wirespec.jetmagic.ui.screens.catselection.CatSelectionScreenHandler
import dev.wirespec.jetmagic.ui.screens.deeplink.DeepLinkHandler
import dev.wirespec.jetmagic.ui.screens.deeplink.DeepLinkScreenHandler
import dev.wirespec.jetmagic.ui.screens.main.MainViewModel
import dev.wirespec.jetmagic.ui.screens.petdetails.PetDetailsHandler
import dev.wirespec.jetmagic.ui.screens.petdetails.PetDetailsScreenHandler
import dev.wirespec.jetmagic.ui.screens.petdetails.PetDetailsViewModel
import dev.wirespec.jetmagic.ui.screens.petslist.PetsListHandler
import dev.wirespec.jetmagic.ui.screens.petslist.PetsListScreenHandler
import dev.wirespec.jetmagic.ui.screens.petslist.PetsListViewModel
import dev.wirespec.jetmagic.ui.screens.petslist.xlarge.land.PetsListWithDetailsScreenHandler
import dev.wirespec.jetmagic.ui.screens.prompttogoback.PromptToGoBackHandler
import dev.wirespec.jetmagic.ui.screens.prompttogoback.PromptToGoBackScreenHandler
import dev.wirespec.jetmagic.ui.screens.prompttogoback.PromptToGoBackScreenViewModel
import dev.wirespec.jetmagic.ui.screens.test.TestHandler
import dev.wirespec.jetmagic.ui.screens.test.TestScreenHandler
import dev.wirespec.jetmagic.ui.screens.test.TestViewModel
import dev.wirespec.jetmagic.ui.screens.unknowndeeplink.UnknownDeepLinkHandler
import dev.wirespec.jetmagic.ui.screens.unknowndeeplink.UnknownDeepLinkScreenHandler


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
                        dev.wirespec.jetmagic.ui.screens.petdetails.land.PetDetailsHandler(composableInstance)
                    },
                    ComposableResource(
                        resourceId = ComposableResourceIDs.PetDetails,
                        viewmodelClass = PetDetailsViewModel::class.java,
                        languageAndRegion = "de",
                    ) { composableInstance ->
                        // PetDetails in German.
                        dev.wirespec.jetmagic.ui.screens.petdetails.de.PetDetailsHandler(composableInstance)
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
    }

    // Returns the current activity.
    var currentActivity: Activity?
        get() = activityLifecycleTracker.currentActivity
        private set(value) {}

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

