package dev.wirespec.sample.ui.nav

import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ScaffoldState
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.wirespec.sample.R
import dev.wirespec.sample.ui.ComposableResourceIDs
import dev.wirespec.sample.ui.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun NavDrawerHandler(scaffoldState: ScaffoldState, modifier: Modifier = Modifier) {

    val vm: NavDrawerViewModel = viewModel()
    val currentMenuId = vm.currentMenuId.observeAsState(NavMenuConstants.MenuHome)

    val scrollState = vm.navDrawerScrollState
    val coroutineScope = rememberCoroutineScope()

    NavDrawer(
        currentMenuId.value,
        scrollState,
        onNavItemClick = { menuId, screen, screenData ->
            coroutineScope.launch {
                scaffoldState.drawerState.close()
                vm.onNavItemClick(menuId, screen, screenData)
            }
        },
        modifier = modifier
    )
}

@Composable
fun NavDrawer(
    currentMenuId: String,
    navDrawerScrollState: ScrollState,
    onNavItemClick: (menuId: String, composableResId: String, screenData: Any?) -> Unit,
    modifier: Modifier = Modifier
) {

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Transparent,
        contentColor = AppTheme.appColorTheme.drawerContent
    ) {
        Row(modifier = modifier.fillMaxSize()) {
            Column(
                modifier = modifier
                    .background(AppTheme.appColorTheme.materialColors.surface)
                    .requiredWidth(200.dp)
                    .fillMaxHeight()
                    .padding(20.dp)
                    .verticalScroll(navDrawerScrollState)
            ) {
                NavMenuItem(
                    menuId = NavMenuConstants.MenuHome,
                    icon = Icons.Filled.Home,
                    title = "Home",
                    composableResId = ComposableResourceIDs.PetsList,
                    selected = currentMenuId == NavMenuConstants.MenuHome,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuSettings,
                    icon = Icons.Filled.Settings,
                    title = "Settings",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Settings",
                    selected = currentMenuId == NavMenuConstants.MenuSettings,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuAccount,
                    icon = Icons.Filled.AccountCircle,
                    title = "Account",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Account.",
                    selected = currentMenuId == NavMenuConstants.MenuAccount,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuFavorites,
                    icon = Icons.Filled.Favorite,
                    title = "Favorites",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Favorites",
                    selected = currentMenuId == NavMenuConstants.MenuFavorites,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuExplore,
                    icon = Icons.Filled.Explore,
                    title = "Explore",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Explore",
                    selected = currentMenuId == NavMenuConstants.MenuExplore,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuFeedback,
                    icon = Icons.Filled.Feedback,
                    title = "Feedback",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Feedback",
                    selected = currentMenuId == NavMenuConstants.MenuFeedback,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuRate,
                    icon = Icons.Filled.Grade,
                    title = "Rate",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Rating",
                    selected = currentMenuId == NavMenuConstants.MenuRate,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuHelp,
                    icon = Icons.Filled.Help,
                    title = "Help",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Help",
                    selected = currentMenuId == NavMenuConstants.MenuHelp,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuPrivacy,
                    icon = Icons.Filled.Https,
                    title = "Privacy",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Privacy",
                    selected = currentMenuId == NavMenuConstants.MenuPrivacy,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuGlobalNetwork,
                    icon = Icons.Filled.Language,
                    title = "Global Network",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Global network",
                    selected = currentMenuId == NavMenuConstants.MenuGlobalNetwork,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuNotifications,
                    icon = Icons.Filled.MarkAsUnread,
                    title = "Notifications",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Notifications",
                    selected = currentMenuId == NavMenuConstants.MenuNotifications,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuPricing,
                    icon = Icons.Filled.Paid,
                    title = "Pricing",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Pricing",
                    selected = currentMenuId == NavMenuConstants.MenuPricing,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuPaymentMethod,
                    icon = Icons.Filled.Payment,
                    title = "Payment",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Payment method",
                    selected = currentMenuId == NavMenuConstants.MenuPaymentMethod,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuAnimalCategories,
                    icon = Icons.Filled.Pets,
                    title = "Categories",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Animal Categories",
                    selected = currentMenuId == NavMenuConstants.MenuAnimalCategories,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuChat,
                    icon = Icons.Filled.QuestionAnswer,
                    title = "Chat",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Chat",
                    selected = currentMenuId == NavMenuConstants.MenuChat,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuRescuers,
                    icon = Icons.Filled.Support,
                    title = "Rescuers",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Rescuers",
                    selected = currentMenuId == NavMenuConstants.MenuRescuers,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuCalendar,
                    icon = Icons.Filled.Today,
                    title = "Calendar",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Calendar",
                    selected = currentMenuId == NavMenuConstants.MenuCalendar,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuVerification,
                    icon = Icons.Filled.VerifiedUser,
                    title = "Verification",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Verification",
                    selected = currentMenuId == NavMenuConstants.MenuVerification,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuVideos,
                    icon = Icons.Filled.VideoLibrary,
                    title = "Videos",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Videos",
                    selected = currentMenuId == NavMenuConstants.MenuVideos,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuAudio,
                    icon = Icons.Filled.VolumeDown,
                    title = "Audio Tracks",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Audio Tracks",
                    selected = currentMenuId == NavMenuConstants.MenuAudio,
                    onNavItemClick = onNavItemClick
                )
                NavMenuItem(
                    menuId = NavMenuConstants.MenuLocations,
                    icon = Icons.Filled.LocationOn,
                    title = "Locations",
                    composableResId = ComposableResourceIDs.TestScreen,
                    dstArgs = "Location",
                    selected = currentMenuId == NavMenuConstants.MenuLocations,
                    onNavItemClick = onNavItemClick
                )
            }

            Image(
                modifier = Modifier.fillMaxHeight(),
                painter = painterResource(id = R.drawable.nav_drawer_bg),
                contentDescription = null,
                contentScale = ContentScale.FillHeight
            )
        }
    }
}