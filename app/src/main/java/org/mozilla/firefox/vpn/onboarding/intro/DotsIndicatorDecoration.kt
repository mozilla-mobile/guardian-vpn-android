package org.mozilla.firefox.vpn.onboarding.intro

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import org.mozilla.firefox.vpn.R

class DotsIndicatorDecoration(
    context: Context,
    @ColorInt private val colorActive: Int,
    @ColorInt private val colorInactive: Int
) : RecyclerView.ItemDecoration() {

    private val indicatorItemWidth = context.resources.getDimensionPixelSize(R.dimen.dot_indicator_width)
    private val indicatorItemPadding = context.resources.getDimensionPixelSize(R.dimen.dot_indicator_horizontal_padding)
    private val indicatorHeight = context.resources.getDimensionPixelSize(R.dimen.dot_indicator_bottom_margin)

    private val interpolator = AccelerateDecelerateInterpolator()
    private val paint = Paint()

    init {
        paint.style = Paint.Style.FILL
        paint.isAntiAlias = true
    }

    override fun onDrawOver(
        c: Canvas,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.onDrawOver(c, parent, state)
        val itemCount = parent.adapter?.itemCount ?: return

        // center horizontally, calculate width and subtract half from center
        val totalLength = indicatorItemWidth * itemCount
        val paddingBetweenItems = max(0, itemCount - 1) * indicatorItemPadding
        val indicatorTotalWidth = totalLength + paddingBetweenItems
        val indicatorStartX = (parent.width - indicatorTotalWidth) / 2f

        // center vertically in the allotted space
        val indicatorPosY = parent.height - indicatorHeight / 2f

        drawInactiveIndicators(c, indicatorStartX, indicatorPosY, itemCount)

        // find active page (which should be highlighted)
        val layoutManager = parent.layoutManager as LinearLayoutManager
        val activePosition = layoutManager.findFirstVisibleItemPosition()
        if (activePosition == RecyclerView.NO_POSITION) {
            return
        }

        // find offset of active page (if the user is scrolling)
        val activeChild = layoutManager.findViewByPosition(activePosition) ?: return
        val left = activeChild.left
        val width = activeChild.width

        // on swipe the active item will be positioned from [-width, 0]
        // interpolate offset for smooth animation
        val progress = interpolator.getInterpolation(left * -1 / width.toFloat())

        drawHighlights(c, indicatorStartX, indicatorPosY, activePosition, progress)
    }

    private fun drawInactiveIndicators(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        itemCount: Int
    ) {
        paint.color = colorInactive

        // width of item indicator including padding
        val itemWidth = indicatorItemWidth + indicatorItemPadding

        var start = indicatorStartX
        for (i in 0 until itemCount) {
            c.drawCircle(start, indicatorPosY, indicatorItemWidth / 2f, paint)
            start += itemWidth
        }
    }

    private fun drawHighlights(
        c: Canvas,
        indicatorStartX: Float,
        indicatorPosY: Float,
        highlightPosition: Int,
        progress: Float
    ) {
        paint.color = colorActive

        // width of item indicator including padding
        val itemWidth = indicatorItemWidth + indicatorItemPadding

        if (progress == 0f) {
            // no swipe, draw a normal indicator
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            c.drawCircle(highlightStart, indicatorPosY, indicatorItemWidth / 2f, paint)
        } else {
            val highlightStart = indicatorStartX + itemWidth * highlightPosition
            // calculate partial highlight
            val partialLength = indicatorItemWidth * progress + indicatorItemPadding * progress
            c.drawCircle(highlightStart + partialLength,
                indicatorPosY,
                indicatorItemWidth / 2f,
                paint)
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.bottom = indicatorHeight
    }
}
