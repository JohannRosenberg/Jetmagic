package dev.wirespec.jetmagic.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager

@SuppressLint("StaticFieldLeak")
class ScreenUtils {
    companion object {
        var ctx: Context? = null

        fun setContext(ctx: Context) {
            this.ctx = ctx
        }

        val context: Context
            get() {
                return ctx as Context
            }

        fun dpToPx(dp: Float): Float {
            return dp * Resources.getSystem().displayMetrics.density
        }

        fun pxToDp(px: Int): Float {
            return px / Resources.getSystem().displayMetrics.density
        }

        fun availableScreenSizeInDp(): Pair<Float, Float> {
            val point = Point()
            (context.getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay.getRealSize(point)
            val displayMetrics: DisplayMetrics = context.resources.displayMetrics
            return Pair(pxToDp(displayMetrics.widthPixels), pxToDp(displayMetrics.heightPixels))
        }
    }
}