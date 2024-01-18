package io.github.johannrosenberg.jetmagic.composables

import android.util.DisplayMetrics

/**
 * See [https://developer.android.com/guide/topics/resources/providing-resources#AlternativeResources] for
 * a list of configuration qualifiers and how they are used.
 */
sealed class QualifierConfiguration {
    object MCCAndMNC : QualifierConfiguration()
    object LanguageAndRegion : QualifierConfiguration()
    object LayoutDirection : QualifierConfiguration()
    object SmallestWidth : QualifierConfiguration()
    object AvailableWidth : QualifierConfiguration()
    object AvailableHeight : QualifierConfiguration()
    object ScreenSize : QualifierConfiguration()
    object ScreenAspect : QualifierConfiguration()
    object RoundScreen : QualifierConfiguration()
    object WideColorGamut : QualifierConfiguration()
    object HighDynamicRange : QualifierConfiguration()
    object ScreenOrientation : QualifierConfiguration()
    object UIMode : QualifierConfiguration()
    object NightMode : QualifierConfiguration()
    object ScreenPixelDensityDpi : QualifierConfiguration()
    object TouchScreenType : QualifierConfiguration()
    object KeyboardAvailibility : QualifierConfiguration()
    object PrimaryTextInputMethod : QualifierConfiguration()
    object NavigationKeyAvailibility : QualifierConfiguration()
    object PrimaryNonTouchNavigationMethod : QualifierConfiguration()
    object PlatformVersion : QualifierConfiguration()
}

sealed class LayoutDirection {
    object LeftToRight : LayoutDirection()
    object RightToLeft : LayoutDirection()
}

sealed class ScreenSize {
    object Small : ScreenSize()
    object Normal : ScreenSize()
    object Large : ScreenSize()
    object XLarge : ScreenSize()
}

sealed class ScreenAspect {
    object Long : ScreenAspect()
    object NotLong : ScreenAspect()
}

sealed class RoundScreen {
    object Round : RoundScreen()
    object NotRound : RoundScreen()
}

sealed class WideColorGamut {
    object WideCG : WideColorGamut()
    object NoWideCG : WideColorGamut()
}

sealed class HighDynamicRange {
    object HighDR : HighDynamicRange()
    object LowDR : HighDynamicRange()
}

sealed class ScreenOrientation {
    object Portrait : ScreenOrientation()
    object Landscape : ScreenOrientation()
}

sealed class UIMode {
    object Car : UIMode()
    object Desk : UIMode()
    object Television : UIMode()
    object Appliance : UIMode()
    object Watch : UIMode()
    object VRHeadset : UIMode()
}

sealed class NightMode {
    object Night : NightMode()
    object NotNight : NightMode()
}

object ScreenPixelDensityDpi {
    const val AnyDPI = -1
    const val NODPI = 0
    const val LDPI = DisplayMetrics.DENSITY_LOW
    const val MDPI  = DisplayMetrics.DENSITY_MEDIUM
    const val HDPI= DisplayMetrics.DENSITY_HIGH
    const val XHDPI= DisplayMetrics.DENSITY_XHIGH
    const val XXHDPI = DisplayMetrics.DENSITY_XXHIGH
    const val XXXHDPI= DisplayMetrics.DENSITY_XXXHIGH
    const val TVDPI= DisplayMetrics.DENSITY_TV
}

sealed class TouchScreenType {
    object NoTouch : TouchScreenType()
    object Finger : TouchScreenType()
}

sealed class KeyboardAvailability {
    object KeysExposed : KeyboardAvailability()
    object KeysHidden : KeyboardAvailability()
    object KeysSoft : KeyboardAvailability()
}

sealed class PrimaryTextInputMethod {
    object NoKeys : PrimaryTextInputMethod()
    object Qwerty : PrimaryTextInputMethod()
    object TwelveKey : PrimaryTextInputMethod()
}

sealed class NavigationKeyAvailibility {
    object NavExposed : NavigationKeyAvailibility()
    object NavHidden : NavigationKeyAvailibility()
}

sealed class PrimaryNonTouchNavigationMethod {
    object NoNav : PrimaryNonTouchNavigationMethod()
    object DPad : PrimaryNonTouchNavigationMethod()
    object Trackball : PrimaryNonTouchNavigationMethod()
    object Wheel : PrimaryNonTouchNavigationMethod()
}