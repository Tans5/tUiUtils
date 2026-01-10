package com.tans.tuiutils.state

interface ValueStorage {

    operator fun <T : Any> get(key: String): T?

    fun <T : Any> getOrPut(key: String, createNew: () -> T): T

    operator fun <T : Any> set(key: String, value: T)

    fun <T : Any> put(key: String, value: T) : T?

    fun remove(key: String)

    fun containsKey(key: String): Boolean

    fun clean()
}