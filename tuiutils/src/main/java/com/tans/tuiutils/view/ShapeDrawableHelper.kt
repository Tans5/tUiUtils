package com.tans.tuiutils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.drawable.GradientDrawable
import com.tans.tuiutils.R

object ShapeDrawableHelper {

    private val ATTRS = intArrayOf(
        R.attr.shapeDrawable,
        R.attr.shapeCornerRadius,
        R.attr.shapeCornerTopLeftRadius,
        R.attr.shapeCornerTopRightRadius,
        R.attr.shapeCornerBottomLeftRadius,
        R.attr.shapeCornerBottomRightRadius,
        R.attr.shapeSolidColor,
        R.attr.shapeGradientStartColor,
        R.attr.shapeGradientCenterColor,
        R.attr.shapeGradientEndColor,
        R.attr.shapeGradientType,
        R.attr.shapeGradientAngle,
        R.attr.shapeGradientCenterX,
        R.attr.shapeGradientCenterY,
        R.attr.shapeGradientRadius,
        R.attr.shapeStrokeWidth,
        R.attr.shapeStrokeColor,
        R.attr.shapeStrokeDashWidth,
        R.attr.shapeStrokeDashGap,
        R.attr.shapeSizeWidth,
        R.attr.shapeSizeHeight,
        R.attr.shapeUseLevel,
    )

    private const val IDX_SHAPE = 0
    private const val IDX_CORNER_RADIUS = 1
    private const val IDX_CORNER_TL = 2
    private const val IDX_CORNER_TR = 3
    private const val IDX_CORNER_BL = 4
    private const val IDX_CORNER_BR = 5
    private const val IDX_SOLID = 6
    private const val IDX_GRADIENT_START = 7
    private const val IDX_GRADIENT_CENTER = 8
    private const val IDX_GRADIENT_END = 9
    private const val IDX_GRADIENT_TYPE = 10
    private const val IDX_GRADIENT_ANGLE = 11
    private const val IDX_GRADIENT_CX = 12
    private const val IDX_GRADIENT_CY = 13
    private const val IDX_GRADIENT_RADIUS = 14
    private const val IDX_STROKE_WIDTH = 15
    private const val IDX_STROKE_COLOR = 16
    private const val IDX_STROKE_DASH_W = 17
    private const val IDX_STROKE_DASH_G = 18
    private const val IDX_SIZE_W = 19
    private const val IDX_SIZE_H = 20
    private const val IDX_USE_LEVEL = 21

    fun apply(view: View, context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        val ta = context.obtainStyledAttributes(attrs, ATTRS, defStyleAttr, defStyleRes)
        val shapeValue = ta.getInt(IDX_SHAPE, -1)
        val needDrawable = shapeValue != -1 || ta.hasValue(IDX_CORNER_RADIUS) ||
                ta.hasValue(IDX_CORNER_TL) || ta.hasValue(IDX_CORNER_TR) ||
                ta.hasValue(IDX_CORNER_BL) || ta.hasValue(IDX_CORNER_BR) ||
                ta.hasValue(IDX_SOLID) || ta.hasValue(IDX_GRADIENT_START) ||
                ta.hasValue(IDX_GRADIENT_CENTER) || ta.hasValue(IDX_GRADIENT_END) ||
                ta.hasValue(IDX_STROKE_WIDTH) || ta.hasValue(IDX_STROKE_COLOR) ||
                ta.hasValue(IDX_SIZE_W) || ta.hasValue(IDX_SIZE_H)
        if (!needDrawable) {
            ta.recycle()
            return
        }
        val drawable = GradientDrawable()
        val shape = when (shapeValue) {
            0 -> GradientDrawable.RECTANGLE
            1 -> GradientDrawable.OVAL
            2 -> GradientDrawable.LINE
            3 -> GradientDrawable.RING
            else -> GradientDrawable.RECTANGLE
        }
        drawable.shape = shape

        val cornerRadius = ta.getDimension(IDX_CORNER_RADIUS, 0f)
        if (cornerRadius > 0f) {
            drawable.cornerRadius = cornerRadius
        } else {
            val tl = ta.getDimension(IDX_CORNER_TL, 0f)
            val tr = ta.getDimension(IDX_CORNER_TR, 0f)
            val br = ta.getDimension(IDX_CORNER_BR, 0f)
            val bl = ta.getDimension(IDX_CORNER_BL, 0f)
            if (tl > 0f || tr > 0f || br > 0f || bl > 0f) {
                drawable.cornerRadii = floatArrayOf(tl, tl, tr, tr, br, br, bl, bl)
            }
        }

        val hasGradient = ta.hasValue(IDX_GRADIENT_START) || ta.hasValue(IDX_GRADIENT_END)
        if (hasGradient) {
            val start = ta.getColor(IDX_GRADIENT_START, 0)
            val centerHas = ta.hasValue(IDX_GRADIENT_CENTER)
            val center = ta.getColor(IDX_GRADIENT_CENTER, 0)
            val end = ta.getColor(IDX_GRADIENT_END, 0)
            val colors = if (centerHas) intArrayOf(start, center, end) else intArrayOf(start, end)
            drawable.colors = colors
            val type = ta.getInt(IDX_GRADIENT_TYPE, 0)
            drawable.gradientType = type
            val angle = ta.getInt(IDX_GRADIENT_ANGLE, 0)
            drawable.orientation = orientationFromAngle(angle)
            val centerX = ta.getFloat(IDX_GRADIENT_CX, 0.5f)
            val centerY = ta.getFloat(IDX_GRADIENT_CY, 0.5f)
            drawable.setGradientCenter(centerX, centerY)
            val radius = ta.getDimension(IDX_GRADIENT_RADIUS, 0f)
            if (radius > 0f) {
                drawable.gradientRadius = radius
            }
        } else {
            if (ta.hasValue(IDX_SOLID)) {
                val solid = ta.getColor(IDX_SOLID, 0)
                drawable.setColor(solid)
            }
        }

        val strokeWidth = ta.getDimensionPixelSize(IDX_STROKE_WIDTH, 0)
        if (strokeWidth > 0) {
            val strokeColor = ta.getColor(IDX_STROKE_COLOR, 0)
            val dashWidth = ta.getDimension(IDX_STROKE_DASH_W, 0f)
            val dashGap = ta.getDimension(IDX_STROKE_DASH_G, 0f)
            if (dashWidth > 0f && dashGap > 0f) {
                drawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
            } else {
                drawable.setStroke(strokeWidth, strokeColor)
            }
        }

        val sizeW = ta.getDimensionPixelSize(IDX_SIZE_W, 0)
        val sizeH = ta.getDimensionPixelSize(IDX_SIZE_H, 0)
        if (sizeW > 0 || sizeH > 0) {
            drawable.setSize(sizeW, sizeH)
        }
        if (ta.hasValue(IDX_USE_LEVEL)) {
            val useLevel = ta.getBoolean(IDX_USE_LEVEL, false)
            drawable.setUseLevel(useLevel)
        }

        ta.recycle()
        view.background = drawable
    }

    private fun orientationFromAngle(angle: Int): GradientDrawable.Orientation {
        val a = ((angle % 360) + 360) % 360
        return when (a) {
            0 -> GradientDrawable.Orientation.LEFT_RIGHT
            45 -> GradientDrawable.Orientation.BL_TR
            90 -> GradientDrawable.Orientation.BOTTOM_TOP
            135 -> GradientDrawable.Orientation.BR_TL
            180 -> GradientDrawable.Orientation.RIGHT_LEFT
            225 -> GradientDrawable.Orientation.TR_BL
            270 -> GradientDrawable.Orientation.TOP_BOTTOM
            315 -> GradientDrawable.Orientation.TL_BR
            else -> GradientDrawable.Orientation.LEFT_RIGHT
        }
    }
}