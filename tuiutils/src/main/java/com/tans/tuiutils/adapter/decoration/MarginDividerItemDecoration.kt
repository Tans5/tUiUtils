package com.tans.tuiutils.adapter.decoration

import android.graphics.*
import android.graphics.drawable.Drawable
import android.view.View
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView

/**
 *
 * author: pengcheng.tan
 * date: 2019-12-23
 */

typealias DividerController = (child: View, parent: RecyclerView, state: RecyclerView.State) -> Boolean

class IgnoreGridLastLineHorizontalDividerController(private val rowSize: Int) : DividerController {

    override fun invoke(child: View, parent: RecyclerView, state: RecyclerView.State): Boolean {
        val holder = parent.getChildViewHolder(child)
        val itemCount = state.itemCount
        val lastLineSize = itemCount % rowSize
        return holder.layoutPosition !in itemCount - lastLineSize until itemCount
    }
}

class IgnoreGridLastRowVerticalDividerController(private val rowSize: Int,
                                                 private val ignoreLastItem: Boolean = false) : DividerController {

    override fun invoke(child: View, parent: RecyclerView, state: RecyclerView.State): Boolean {
        val holder = parent.getChildViewHolder(child)
        return if (ignoreLastItem && holder.layoutPosition == state.itemCount - 1) {
            false
        } else {
            (holder.layoutPosition + 1) % rowSize != 0
        }
    }

}

val ignoreLastDividerController: DividerController = { child, parent, state ->
    val holder = parent.getChildViewHolder(child)
    holder.layoutPosition != state.itemCount - 1
}

class MarginDividerItemDecoration(
    val divider: Divider,
    val marginStart: Int,
    val marginEnd: Int,
    val dividerDirection: DividerDirection,
    val dividerController: DividerController
) : RecyclerView.ItemDecoration() {


    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val childCount: Int = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            if (!dividerController(child, parent, state)) {
                continue
            }
            val bounds = Rect()
            parent.layoutManager?.getDecoratedBoundsWithMargins(child, bounds)
                ?: error("LayoutManager is null")
            c.save()
            val left = when (dividerDirection) {
                DividerDirection.Horizontal -> marginStart + bounds.left
                DividerDirection.Vertical -> bounds.right - divider.size
            }
            val top = when (dividerDirection) {
                DividerDirection.Horizontal -> bounds.bottom - divider.size
                DividerDirection.Vertical -> bounds.top + marginStart
            }
            val right = when (dividerDirection) {
                DividerDirection.Horizontal -> bounds.right - marginEnd
                DividerDirection.Vertical -> bounds.right
            }
            val bottom = when (dividerDirection) {
                DividerDirection.Horizontal -> bounds.bottom
                DividerDirection.Vertical -> bounds.bottom - marginEnd
            }
            c.clipRect(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
            c.translate(left.toFloat(), top.toFloat())
            divider.onDraw(canvas = c, width = right - left, height = bottom - top, vh = parent.getChildViewHolder(child))
            c.restore()
        }
    }

    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        if (dividerController(view, parent, state)) {
            when (dividerDirection) {
                DividerDirection.Horizontal -> {
                    outRect.set(0, 0, 0, divider.size)
                }
                DividerDirection.Vertical -> {
                    outRect.set(0, 0, divider.size, 0)
                }
            }
        } else {
            outRect.set(0, 0, 0, 0)
        }
    }


    companion object {


        enum class DividerDirection { Horizontal, Vertical }

        class Builder {

            private var divider: Divider = ColorDivider(color = Color.rgb(66, 66, 66), size = 2)
            private var marginStart: Int = 0
            private var marginEnd: Int = 0
            private var dividerDirection: DividerDirection = DividerDirection.Horizontal
            private var dividerController: DividerController = { _, _, _ -> true }

            fun divider(divider: Divider): Builder {
                this.divider = divider
                return this
            }

            fun marginStart(marginStart: Int): Builder {
                this.marginStart = marginStart
                return this
            }

            fun marginEnd(marginEnd: Int): Builder {
                this.marginEnd = marginEnd
                return this
            }

            fun dividerDirection(dividerDirection: DividerDirection): Builder {
                this.dividerDirection = dividerDirection
                return this
            }

            fun dividerController(dividerController: DividerController): Builder {
                this.dividerController = dividerController
                return this
            }

            fun build(): MarginDividerItemDecoration {
                return MarginDividerItemDecoration(
                    divider = divider,
                    marginStart = marginStart,
                    marginEnd = marginEnd,
                    dividerDirection = dividerDirection,
                    dividerController = dividerController
                )
            }

        }

        interface Divider {

            // pixel size, divider width or height.
            val size: Int

            fun onDraw(canvas: Canvas, width: Int, height: Int, vh: RecyclerView.ViewHolder)

        }

        class ColorDivider(
            @ColorInt val color: Int,
            override val size: Int
        ) : Divider {

            private val paint: Paint = Paint().apply {
                color = this@ColorDivider.color
                style = Paint.Style.FILL
                isAntiAlias = true
            }

            override fun onDraw(canvas: Canvas, width: Int, height: Int, vh: RecyclerView.ViewHolder) {
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            }
        }

        class DrawableDivider(
            val drawable: Drawable,
            override val size: Int
        ) : Divider {

            override fun onDraw(canvas: Canvas, width: Int, height: Int, vh: RecyclerView.ViewHolder) {
                drawable.setBounds(0, 0, width, height)
                drawable.draw(canvas)
            }
        }
    }

}