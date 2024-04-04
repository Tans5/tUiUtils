@file:Suppress("UNCHECKED_CAST")

package com.tans.tuiutils.adapter.impl.builders

import androidx.recyclerview.widget.RecyclerView
import com.tans.tuiutils.adapter.AdapterBuilder


class CombinedAdapterBuilderImpl(internal val childrenBuilders: List<AdapterBuilder<Any>>) {

    fun build(): RecyclerView.Adapter<*> {
        return CombinedAdapterImpl(this)
    }
}

infix operator fun <A : Any, B : Any> AdapterBuilder<A>.plus(b: AdapterBuilder<B>): CombinedAdapterBuilderImpl {
    return if (this == b) {
        CombinedAdapterBuilderImpl(listOf(this) as List<AdapterBuilder<Any>>)
    } else {
        CombinedAdapterBuilderImpl(listOf(this, b) as List<AdapterBuilder<Any>>)
    }
}

infix operator fun <A : Any> CombinedAdapterBuilderImpl.plus(b: AdapterBuilder<A>): CombinedAdapterBuilderImpl {
    return if (childrenBuilders.contains(b as AdapterBuilder<Any>)) {
        this
    } else {
        CombinedAdapterBuilderImpl(this.childrenBuilders + (b as AdapterBuilder<Any>))
    }
}

infix operator fun <A : Any> AdapterBuilder<A>.plus(b: CombinedAdapterBuilderImpl): CombinedAdapterBuilderImpl {
    return if (b.childrenBuilders.contains(this as AdapterBuilder<Any>)) {
        b
    } else {
        CombinedAdapterBuilderImpl(listOf(this as AdapterBuilder<Any>) + b.childrenBuilders)
    }
}