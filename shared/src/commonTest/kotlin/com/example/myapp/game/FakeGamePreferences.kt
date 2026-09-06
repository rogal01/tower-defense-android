package com.example.myapp.game

class FakeGamePreferences : GamePreferences {
    private val storage = mutableMapOf<String, Any>()

    override fun getInt(key: String, default: Int): Int =
        (storage[key] as? Int) ?: default

    override fun getLong(key: String, default: Long): Long =
        (storage[key] as? Long) ?: (storage[key] as? Int)?.toLong() ?: default

    override fun getFloat(key: String, default: Float): Float =
        (storage[key] as? Float) ?: default

    override fun getBoolean(key: String, default: Boolean): Boolean =
        (storage[key] as? Boolean) ?: default

    override fun getString(key: String, default: String): String =
        (storage[key] as? String) ?: default

    override fun edit(): GamePreferences.Editor = FakeEditor()

    fun getAll(): Map<String, Any> = storage.toMap()

    fun clear() {
        storage.clear()
    }

    private inner class FakeEditor : GamePreferences.Editor {
        private val temp = mutableMapOf<String, Any>()

        override fun putInt(key: String, value: Int): GamePreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putLong(key: String, value: Long): GamePreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putFloat(key: String, value: Float): GamePreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putBoolean(key: String, value: Boolean): GamePreferences.Editor {
            temp[key] = value
            return this
        }

        override fun putString(key: String, value: String): GamePreferences.Editor {
            temp[key] = value
            return this
        }

        override fun apply() {
            storage.putAll(temp)
        }
    }
}
