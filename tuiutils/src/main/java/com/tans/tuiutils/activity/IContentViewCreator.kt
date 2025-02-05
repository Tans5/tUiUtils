package com.tans.tuiutils.activity

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes

interface IContentViewCreator {

    @get:LayoutRes
    val layoutId: Int

    fun createContentView(context: Context, parentView: ViewGroup?): View? {
        return null
    }

}


fun IContentViewCreator.tryCreateNewContentView(context: Context, parentView: ViewGroup?): View? {
    return if (layoutId != 0) {
        LayoutInflater.from(context).inflate(layoutId, parentView, false)
    } else {
        createContentView(context, parentView)
    }
}

