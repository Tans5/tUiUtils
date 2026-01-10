package com.tans.tuiutils.state

import java.util.concurrent.ConcurrentHashMap

@Suppress("UNCHECKED_CAST")
class SimpleValueStorage : ValueStorage {

    private val map : MutableMap<String, Any> by lazy {
        ConcurrentHashMap<String, Any>()
    }

    override operator fun <T : Any> get(key: String): T? {
        return map[key] as? T
    }

    override fun <T : Any> getOrPut(key: String, createNew: () -> T): T {
        return map.getOrPut(key) { createNew() } as T
    }

    override operator fun <T : Any> set(key: String, value: T) {
        map[key] = value
    }

    override fun <T : Any> put(key: String, value: T) : T? {
        return map.put(key, value) as? T
    }

    override fun remove(key: String) {
        map.remove(key)
    }

    override fun containsKey(key: String): Boolean {
        return map.containsKey(key)
    }

    override fun clean() {
        map.clear()
    }
}