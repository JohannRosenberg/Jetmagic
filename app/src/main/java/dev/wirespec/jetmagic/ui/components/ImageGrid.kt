package dev.wirespec.jetmagic.ui.components

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.wirespec.jetmagic.utils.DeviceUtils
import dev.wirespec.jetmagic.utils.DeviceUtils.Companion.dp2px
import dev.wirespec.jetmagic.utils.DeviceUtils.Companion.px2dp


/**
 * Displays a grid of images. The number of items in the list is fixed. The number of columns
 * displayed is adaptive while the size of grid items is fixed with a smaller size used on
 * phones and a larger size used on tablets.
 */

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@ExperimentalAnimationApi
@Composable
fun <T> ImageGrid(
    modifier: Modifier = Modifier,
    items: List<T>,
    itemContent: @Composable (item: T) -> Unit
) {
    BoxWithConstraints(modifier = modifier) {
        val preferredCellSize = if (DeviceUtils.isATablet()) {
            200.dp2px
        } else {
            90.dp2px
        }

        // Calculate how many cells (columns) fit on a row.
        val spacing = if (DeviceUtils.displayMetrics.densityDpi >= 480) {
            3.dp2px
        } else {
            5.dp2px
        }

        val totalColumns = constraints.maxWidth / (preferredCellSize + spacing)

        // Cell width does not include the spacing. Calculation values are done using pixels and NOT dp.
        val cellWidth = (constraints.maxWidth - (totalColumns - 1) * spacing) / totalColumns
        var totalRows = items.size / totalColumns

        if (items.size % totalColumns > 0) {
            totalRows++
        }

        // When a row of cells is created, there will normally be a small amount of spacing to the right
        // of the last cell left over (on the right side of the grid). That's because the total amount of space
        // occupied by cells plus their divider doesn't divide into the available space equally.
        // Instead of leaving a vertical empty space along the right side of the grid, just increase the
        // width of all cells in the last column.

        val lastColumnWidth = cellWidth + (constraints.maxWidth - ((totalColumns * cellWidth) + ((totalColumns - 1) * spacing)))

        Layout(
            modifier = modifier,
            content = {
                ImageGridItems(
                    items = items,
                    totalColumns = totalColumns,
                    size = cellWidth.px2dp.dp,
                    lastColumnWidth = lastColumnWidth.px2dp.dp,
                    itemContent = itemContent
                )
            }
        ) { measurables, constraints ->
            val placeables = measurables.map { measurable ->
                measurable.measure(constraints)
            }

            val height = (cellWidth * totalRows) + (spacing * (totalRows - 1))

            layout(constraints.maxWidth, height) {
                var x = 0
                var y = 0
                var column = 1

                placeables.forEach { placeable ->
                    placeable.placeRelative(x = x, y = y)

                    if (column == totalColumns) {
                        column = 1
                        x = 0
                        y += (cellWidth + spacing)
                    } else {
                        column++
                        x += (cellWidth + spacing)
                    }
                }
            }
        }
    }
}

/**
 * Displays all the items in the image grid.
 */
@Composable
fun <T> ImageGridItems(
    items: List<T>,
    totalColumns: Int,
    size: Dp,
    lastColumnWidth: Dp,
    itemContent: @Composable (item: T) -> Unit
) {
    var column = 1

    items.forEach {
        val width: Dp

        if (column == totalColumns) {
            width = lastColumnWidth
            column = 1
        } else {
            width = size
        }

        Box(
            modifier = Modifier
                .requiredWidth(width)
                .requiredHeight(size)
        ) {
            itemContent(it)
        }

        column++
    }
}