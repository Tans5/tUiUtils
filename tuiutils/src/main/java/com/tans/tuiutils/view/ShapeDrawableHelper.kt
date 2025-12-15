package com.tans.tuiutils.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.graphics.drawable.GradientDrawable
import com.tans.tuiutils.R

object ShapeDrawableHelper {

    fun apply(view: View, context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        view.clipToOutline = true
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout, defStyleAttr, defStyleRes)
        val shapeValue = ta.getInt(R.styleable.ShapeLayout_shapeDrawable, -1)
        val needDrawable = shapeValue != -1 ||
                ta.hasValue(R.styleable.ShapeLayout_shapeCornerRadius) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeCornerTopLeftRadius) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeCornerTopRightRadius) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeCornerBottomLeftRadius) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeCornerBottomRightRadius) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeSolidColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeGradientStartColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeGradientCenterColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeGradientEndColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeStrokeWidth) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeStrokeColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeSizeWidth) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeSizeHeight) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeUseLevel)
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
        val cornerRadius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerRadius, 0f)
        val tl = ta.getDimension(R.styleable.ShapeLayout_shapeCornerTopLeftRadius, 0f)
        val tr = ta.getDimension(R.styleable.ShapeLayout_shapeCornerTopRightRadius, 0f)
        val br = ta.getDimension(R.styleable.ShapeLayout_shapeCornerBottomRightRadius, 0f)
        val bl = ta.getDimension(R.styleable.ShapeLayout_shapeCornerBottomLeftRadius, 0f)
        if (cornerRadius > 0f) {
            drawable.cornerRadius = cornerRadius
        } else if (tl > 0f || tr > 0f || br > 0f || bl > 0f) {
            drawable.cornerRadii = floatArrayOf(tl, tl, tr, tr, br, br, bl, bl)
        }
        val hasGradient = ta.hasValue(R.styleable.ShapeLayout_shapeGradientStartColor) ||
                ta.hasValue(R.styleable.ShapeLayout_shapeGradientEndColor)
        if (hasGradient) {
            val start = ta.getColor(R.styleable.ShapeLayout_shapeGradientStartColor, 0)
            val centerHas = ta.hasValue(R.styleable.ShapeLayout_shapeGradientCenterColor)
            val center = ta.getColor(R.styleable.ShapeLayout_shapeGradientCenterColor, 0)
            val end = ta.getColor(R.styleable.ShapeLayout_shapeGradientEndColor, 0)
            drawable.colors = if (centerHas) intArrayOf(start, center, end) else intArrayOf(start, end)
            val type = ta.getInt(R.styleable.ShapeLayout_shapeGradientType, 0)
            drawable.gradientType = type
            val angle = ta.getInt(R.styleable.ShapeLayout_shapeGradientAngle, 0)
            drawable.orientation = ShapeDrawableHelper.orientationFromAngle(angle)
            val centerX = ta.getFloat(R.styleable.ShapeLayout_shapeGradientCenterX, 0.5f)
            val centerY = ta.getFloat(R.styleable.ShapeLayout_shapeGradientCenterY, 0.5f)
            drawable.setGradientCenter(centerX, centerY)
            val radius = ta.getDimension(R.styleable.ShapeLayout_shapeGradientRadius, 0f)
            if (radius > 0f) drawable.gradientRadius = radius
        } else if (ta.hasValue(R.styleable.ShapeLayout_shapeSolidColor)) {
            val solid = ta.getColor(R.styleable.ShapeLayout_shapeSolidColor, 0)
            drawable.setColor(solid)
        }
        val strokeWidth = ta.getDimensionPixelSize(R.styleable.ShapeLayout_shapeStrokeWidth, 0)
        if (strokeWidth > 0) {
            val strokeColor = ta.getColor(R.styleable.ShapeLayout_shapeStrokeColor, 0)
            val dashWidth = ta.getDimension(R.styleable.ShapeLayout_shapeStrokeDashWidth, 0f)
            val dashGap = ta.getDimension(R.styleable.ShapeLayout_shapeStrokeDashGap, 0f)
            if (dashWidth > 0f && dashGap > 0f) {
                drawable.setStroke(strokeWidth, strokeColor, dashWidth, dashGap)
            } else {
                drawable.setStroke(strokeWidth, strokeColor)
            }
        }
        val sizeW = ta.getDimensionPixelSize(R.styleable.ShapeLayout_shapeSizeWidth, 0)
        val sizeH = ta.getDimensionPixelSize(R.styleable.ShapeLayout_shapeSizeHeight, 0)
        if (sizeW > 0 || sizeH > 0) drawable.setSize(sizeW, sizeH)
        if (ta.hasValue(R.styleable.ShapeLayout_shapeUseLevel)) {
            val useLevel = ta.getBoolean(R.styleable.ShapeLayout_shapeUseLevel, false)
            drawable.setUseLevel(useLevel)
        }
        ta.recycle()
        view.background = drawable
    }

    fun orientationFromAngle(angle: Int): GradientDrawable.Orientation {
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