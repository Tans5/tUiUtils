package com.tans.tuiutils.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.LinearLayout

class ShapeLinearLayout : LinearLayout, ShapeLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        applyAttrs(attrs)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttrs(attrs)
    }

    private var attrs: AttributeSet? = null

    override val shapeLayoutParams: ShapeLayoutHelper.ShapeLayoutParams? by lazy {
        ShapeLayoutHelper.parseShapeParams(context, attrs)
    }

    private fun applyAttrs(attrs: AttributeSet?) {
        this.attrs = attrs
        setupBackground(this)
    }

    override fun dispatchDraw(canvas: Canvas) {
        canvas.save()
        clipContent(this, canvas)
        super.dispatchDraw(canvas)
        canvas.restore()
    }
}