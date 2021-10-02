package dev.wirespec.jetmagic.ui.nav

import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material.ripple.RippleAlpha
import androidx.compose.material.ripple.RippleTheme
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.ui.theme.AppColors
import dev.wirespec.jetmagic.ui.theme.AppTheme

@Composable
fun NavMenuItem(
    menuId: String,
    icon: ImageVector,
    title: String,
    composableResId: String,
    modifier: Modifier = Modifier,
    dstArgs: Any? = null,
    selected: Boolean = false,
    onNavItemClick: (menuId: String, screen: String, dstArgs: Any?) -> Unit
) {
    val rippleColor = AppColors.turquoise
    val expectedAlpha = 0.5f
    val rippleAlpha = RippleAlpha(expectedAlpha, expectedAlpha, expectedAlpha, expectedAlpha)

    val rippleTheme = object : RippleTheme {
        @Composable
        override fun defaultColor(): androidx.compose.ui.graphics.Color = rippleColor

        @Composable
        override fun rippleAlpha(): RippleAlpha = rippleAlpha
    }

    CompositionLocalProvider(LocalRippleTheme provides rippleTheme) {
        Row(
            modifier = modifier
                .width(190.dp)
                .indication(indication = rememberRipple(color = rippleColor), interactionSource  = MutableInteractionSource())
                .clickable {
                    onNavItemClick(menuId, composableResId, dstArgs)
                }
                .padding(top = 10.dp, end = 5.dp, bottom = 10.dp, start = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tint = if (selected) AppTheme.appColorTheme.drawerSelected else AppTheme.appColorTheme.drawerContent,
                modifier = modifier.padding(end = 10.dp),
                imageVector = icon,
                contentDescription = ""
            )
            Text(title, color = if (selected) AppTheme.appColorTheme.drawerSelected else AppTheme.appColorTheme.drawerContent)
        }
    }
}