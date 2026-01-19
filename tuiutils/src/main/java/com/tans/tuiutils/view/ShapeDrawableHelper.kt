package com.tans.tuiutils.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.View
import android.graphics.drawable.GradientDrawable
import com.tans.tuiutils.R

interface ShapeLayout {
    val shapeLayoutParams: ShapeLayoutHelper.ShapeLayoutParams?

    fun setupBackground(view: View) {
        if (shapeLayoutParams != null) {
            view.background = ShapeLayoutHelper.createDrawable(view, shapeLayoutParams)
        }
    }

    fun clipContent(view: View, canvas: Canvas) {
        ShapeLayoutHelper.clipContent(view, canvas, shapeLayoutParams)
    }
}
object ShapeLayoutHelper {

    data class ShapeLayoutParams(
        val shape: Int,
        val solidColor: Int,
        val radius: Float,
        val topLeftRadius: Float,
        val topRightRadius: Float,
        val bottomRightRadius: Float,
        val bottomLeftRadius: Float,
        val strokeWidth: Float,
        val strokeColor: Int,
        val gradientType: Int,
        val angle: Int,
        val gradientCenterX: Float,
        val gradientCenterY: Float,
        val gradientRadius: Float,
        val startColor: Int,
        val centerColor: Int,
        val endColor: Int,
        val width: Float,
        val height: Float,
        val useLevel: Boolean,
        val dashWidth: Float,
        val dashGap: Float,
        val clipToOutline: Boolean,
        val hasGradient: Boolean,
        val hasCenterColor: Boolean,
        val hasSolidColor: Boolean,
        val hasUseLevel: Boolean
    )

    fun parseShapeParams(context: Context, attrs: AttributeSet?): ShapeLayoutParams? {
        attrs ?: return null
        val ta = context.obtainStyledAttributes(attrs, R.styleable.ShapeLayout)
        val shapeValue = ta.getInt(R.styleable.ShapeLayout_shapeDrawable, -1)
        val shape = when (shapeValue) {
            0 -> GradientDrawable.RECTANGLE
            1 -> GradientDrawable.OVAL
            2 -> GradientDrawable.LINE
            3 -> GradientDrawable.RING
            else -> GradientDrawable.RECTANGLE
        }
        val solidColor = ta.getColor(R.styleable.ShapeLayout_shapeSolidColor, Color.TRANSPARENT)
        val radius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerRadius, 0F)
        val topLeftRadius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerTopLeftRadius, 0F)
        val topRightRadius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerTopRightRadius, 0F)
        val bottomRightRadius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerBottomRightRadius, 0F)
        val bottomLeftRadius = ta.getDimension(R.styleable.ShapeLayout_shapeCornerBottomLeftRadius, 0F)
        val strokeWidth = ta.getDimension(R.styleable.ShapeLayout_shapeStrokeWidth, 0F)
        val strokeColor = ta.getColor(R.styleable.ShapeLayout_shapeStrokeColor, Color.TRANSPARENT)
        val gradientTypeValue = ta.getInt(R.styleable.ShapeLayout_shapeGradientType, 0)
        val gradientType = when (gradientTypeValue) {
            0 -> GradientDrawable.LINEAR_GRADIENT
            1 -> GradientDrawable.RADIAL_GRADIENT
            2 -> GradientDrawable.SWEEP_GRADIENT
            else -> GradientDrawable.LINEAR_GRADIENT
        }
        val angle = ta.getInt(R.styleable.ShapeLayout_shapeGradientAngle, 0)
        val gradientCenterX = ta.getFloat(R.styleable.ShapeLayout_shapeGradientCenterX, 0.5f)
        val gradientCenterY = ta.getFloat(R.styleable.ShapeLayout_shapeGradientCenterY, 0.5f)
        val gradientRadius = ta.getDimension(R.styleable.ShapeLayout_shapeGradientRadius, 0f)
        val startColor = ta.getColor(R.styleable.ShapeLayout_shapeGradientStartColor, Color.TRANSPARENT)
        val centerColor = ta.getColor(R.styleable.ShapeLayout_shapeGradientCenterColor, Color.TRANSPARENT)
        val endColor = ta.getColor(R.styleable.ShapeLayout_shapeGradientEndColor, Color.TRANSPARENT)
        val width = ta.getDimension(R.styleable.ShapeLayout_shapeSizeWidth, 0f)
        val height = ta.getDimension(R.styleable.ShapeLayout_shapeSizeHeight, 0f)
        val useLevel = ta.getBoolean(R.styleable.ShapeLayout_shapeUseLevel, false)
        val dashWidth = ta.getDimension(R.styleable.ShapeLayout_shapeStrokeDashWidth, 0f)
        val dashGap = ta.getDimension(R.styleable.ShapeLayout_shapeStrokeDashGap, 0f)
        val clipToOutline = ta.getBoolean(R.styleable.ShapeLayout_shapeClipToOutline, false)

        val hasGradient = ta.hasValue(R.styleable.ShapeLayout_shapeGradientStartColor) || ta.hasValue(R.styleable.ShapeLayout_shapeGradientEndColor)
        val hasCenterColor = ta.hasValue(R.styleable.ShapeLayout_shapeGradientCenterColor)
        val hasSolidColor = ta.hasValue(R.styleable.ShapeLayout_shapeSolidColor)
        val hasUseLevel = ta.hasValue(R.styleable.ShapeLayout_shapeUseLevel)
        ta.recycle()
        return ShapeLayoutParams(
            shape = shape,
            solidColor = solidColor,
            radius = radius,
            topLeftRadius = topLeftRadius,
            topRightRadius = topRightRadius,
            bottomRightRadius = bottomRightRadius,
            bottomLeftRadius = bottomLeftRadius,
            strokeWidth = strokeWidth,
            strokeColor = strokeColor,
            gradientType = gradientType,
            angle = angle,
            gradientCenterX = gradientCenterX,
            gradientCenterY = gradientCenterY,
            gradientRadius = gradientRadius,
            startColor = startColor,
            centerColor = centerColor,
            endColor = endColor,
            width = width,
            height = height,
            useLevel = useLevel,
            dashWidth = dashWidth,
            dashGap = dashGap,
            clipToOutline = clipToOutline,
            hasGradient = hasGradient,
            hasCenterColor = hasCenterColor,
            hasSolidColor = hasSolidColor,
            hasUseLevel = hasUseLevel
        )
    }

    fun createDrawable(view: View, params: ShapeLayoutParams?): Drawable? {
        params ?: return null
        return with(params) {

            val drawable = GradientDrawable()
            drawable.shape = shape
            if (radius > 0f) {
                drawable.cornerRadius = radius
            } else if (topLeftRadius > 0f || topRightRadius > 0f || bottomRightRadius > 0f || bottomLeftRadius > 0f) {
                drawable.cornerRadii = floatArrayOf(
                    topLeftRadius, topLeftRadius,
                    topRightRadius, topRightRadius,
                    bottomRightRadius, bottomRightRadius,
                    bottomLeftRadius, bottomLeftRadius
                )
            }
            if (hasGradient) {
                drawable.colors = if (hasCenterColor) {
                    intArrayOf(startColor, centerColor, endColor)
                } else {
                    intArrayOf(startColor, endColor)
                }
                drawable.gradientType = gradientType
                drawable.orientation = orientationFromAngle(angle)
                drawable.setGradientCenter(gradientCenterX, gradientCenterY)
                if (gradientRadius > 0f) {
                    drawable.gradientRadius = gradientRadius
                }
            } else if (hasSolidColor) {
                drawable.setColor(solidColor)
            }
            if (strokeWidth > 0) {
                if (dashWidth > 0f && dashGap > 0f) {
                    drawable.setStroke(strokeWidth.toInt(), strokeColor, dashWidth, dashGap)
                } else {
                    drawable.setStroke(strokeWidth.toInt(), strokeColor)
                }
            }
            if (width > 0 || height > 0) {
                drawable.setSize(width.toInt(), height.toInt())
            }
            if (hasUseLevel) {
                drawable.useLevel = useLevel
            }
            view.clipToOutline = clipToOutline
            drawable
        }
    }

    fun clipContent(view: View, canvas: Canvas, params: ShapeLayoutParams?) {
        params ?: return
        params.apply {
            if (clipToOutline) {
                if (shape == GradientDrawable.RECTANGLE) { // 矩形裁切
                    val radii = if (radius > 0.0f) {
                        floatArrayOf(
                            radius, radius,
                            radius, radius,
                            radius, radius,
                            radius, radius,
                        )
                    } else {
                        floatArrayOf(
                            topLeftRadius, topLeftRadius,
                            topRightRadius, topRightRadius,
                            bottomRightRadius, bottomRightRadius,
                            bottomLeftRadius, bottomLeftRadius
                        )
                    }
                    val path = Path()
                    val rect = RectF(0f, 0f, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
                    path.addRoundRect(rect, radii, Path.Direction.CW)
                    canvas.clipPath(path)
                }

                if (shape == GradientDrawable.RING || shape == GradientDrawable.OVAL) {
                    val path = Path()
                    val rect = RectF(0f, 0f, view.measuredWidth.toFloat(), view.measuredHeight.toFloat())
                    path.addOval(rect, Path.Direction.CW)
                    canvas.clipPath(path)
                }
            }
        }
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