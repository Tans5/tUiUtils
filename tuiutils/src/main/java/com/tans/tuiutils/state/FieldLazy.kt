package com.tans.tuiutils.state


open class FieldLazy<T : Any> : Lazy<T> {

    private val key: String
    private val storageProvider:  () -> ValueStorage
    private val initializer: () -> T

    constructor(
        key: String,
        storageProvider: () -> ValueStorage,
        initializer: () -> T
    ) {
        this.key = key
        this.storageProvider = storageProvider
        this.initializer = initializer
    }

    @Suppress("UNCHECKED_CAST")
    override val value: T
        get() {
            val storage = storageProvider()
            val firstCheckValue: Any? = storage[key]
            var result: T? = null
            // First check.
            if (firstCheckValue != null) {
                try {
                    result = firstCheckValue as T
                } catch (e: Throwable) {
                    error("Wrong field type, maybe you use same key in different fields, key=$key, error=${e.message}")
                }
            } else {
                synchronized(this) {
                    val secondCheckValue: Any? = storage[key]
                    // Second check.
                    result = if (secondCheckValue != null) {
                        try {
                            secondCheckValue as T
                        } catch (e: Throwable) {
                            error("Wrong field type, maybe you use same key in different fields, key=$key, error=${e.message}")
                        }
                    } else {
                        val newValue = initializer()
                        val oldValue = storage.put(key, newValue)
                        if (oldValue == null) {
                            newValue
                        } else {
                            error("Use wrong key: $key, contain old value: $oldValue")
                        }
                    }
                }
            }
            return result!!
        }

    override fun isInitialized(): Boolean {
        return storageProvider().containsKey(key)
    }

}