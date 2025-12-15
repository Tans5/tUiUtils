package com.tans.tuiutils.view

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

class ShapeFrameLayout : FrameLayout {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        applyAttrs(context, attrs, 0, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        applyAttrs(context, attrs, defStyleAttr, 0)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) : super(context, attrs, defStyleAttr, defStyleRes) {
        applyAttrs(context, attrs, defStyleAttr, defStyleRes)
    }

    private fun applyAttrs(context: Context, attrs: AttributeSet?, defStyleAttr: Int, defStyleRes: Int) {
        ShapeDrawableHelper.apply(this, context, attrs, defStyleAttr, defStyleRes)
    }
}