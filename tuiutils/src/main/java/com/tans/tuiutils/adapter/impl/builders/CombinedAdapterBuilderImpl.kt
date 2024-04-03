@file:Suppress("UNCHECKED_CAST")

package com.tans.tuiutils.adapter.impl.builders

import com.tans.tuiutils.adapter.AdapterBuilder


class CombinedAdapterBuilderImpl(internal val childrenBuilders: Set<AdapterBuilder<Any>>)  {

}

infix operator fun <A : Any, B : Any> AdapterBuilder<A>.plus(b: AdapterBuilder<B>): CombinedAdapterBuilderImpl {
    return CombinedAdapterBuilderImpl(setOf(this, b) as Set<AdapterBuilder<Any>>)
}

infix operator fun <A : Any> CombinedAdapterBuilderImpl.plus(b: AdapterBuilder<A>): CombinedAdapterBuilderImpl {
    return if (this.childrenBuilders.contains(b as AdapterBuilder<Any>)) this else CombinedAdapterBuilderImpl(
        this.childrenBuilders + b as AdapterBuilder<Any>
    )
}

infix operator fun <A : Any> AdapterBuilder<A>.plus(b: CombinedAdapterBuilderImpl): CombinedAdapterBuilderImpl {
    return if (b.childrenBuilders.contains(this as AdapterBuilder<Any>)) b else CombinedAdapterBuilderImpl(
        setOf(
            this as AdapterBuilder<Any>
        ) + b.childrenBuilders
    )
}