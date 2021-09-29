package dev.wirespec.jetmagic.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.material.ExperimentalMaterialApi
import dev.wirespec.jetmagic.App
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@ExperimentalFoundationApi
@ExperimentalAnimationApi
class DeviceUtils {
    companion object {
        fun convertDpToPixel(dp: Int): Int {
            val px = dp * (App.context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return px.toInt()
        }

        fun convertPixelsToDp(px: Int): Int {
            val dp = px / (App.context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)
            return dp.toInt()
        }

        val displayMetrics: DisplayMetrics by lazy { Resources.getSystem().displayMetrics }

        val screenRectPx: Rect
            get() = displayMetrics.run { Rect(0, 0, widthPixels, heightPixels) }

        val screenRectDp: RectF
            get() = displayMetrics.run { RectF(0f, 0f, widthPixels.px2dp, heightPixels.px2dp) }

        val Number.px2dp: Float
            get() = this.toFloat() / displayMetrics.density

        val Number.dp2px: Int
            get() = (this.toFloat() * displayMetrics.density).roundToInt()

        /**
         * Returns the screen size in inches. This is the screen's longest diagonal length between two corners.
         */
        fun screenSizeInInches(): Float {
            val point = Point()
            (App.context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(point)
            val displayMetrics: DisplayMetrics = App.context.resources.displayMetrics
            val width: Int = point.x
            val height: Int = point.y
            val wi = width.toDouble() / displayMetrics.xdpi.toDouble()
            val hi = height.toDouble() / displayMetrics.ydpi.toDouble()
            val x = Math.pow(wi, 2.0)
            val y = Math.pow(hi, 2.0)
            return (Math.round(Math.sqrt(x + y) * 10.0) / 10.0).toFloat()
        }

        /**
         * Returns true if the device is a tablet. Generally, anything 7 inches or larger can be considered a tablet.
         */
        fun isATablet(): Boolean {
            return (screenSizeInInches() >= 7)
        }
    }
}