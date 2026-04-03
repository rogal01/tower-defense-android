package com.example.myapp.game

import platform.Foundation.NSUserDefaults

/** iOS implementation of GamePreferences wrapping NSUserDefaults */
class IosGamePreferences(private val suiteName: String) : GamePreferences {

    private val defaults = NSUserDefaults(suiteName = suiteName)

    override fun getInt(key: String, default: Int): Int {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key).toInt() else default
    }
    override fun getLong(key: String, default: Long): Long {
        return if (defaults.objectForKey(key) != null) defaults.integerForKey(key) else default
    }
    override fun getFloat(key: String, default: Float): Float {
        return if (defaults.objectForKey(key) != null) defaults.floatForKey(key) else default
    }
    override fun getBoolean(key: String, default: Boolean): Boolean {
        return if (defaults.objectForKey(key) != null) defaults.boolForKey(key) else default
    }
    override fun getString(key: String, default: String): String {
        return defaults.stringForKey(key) ?: default
    }

    override fun edit(): GamePreferences.Editor = IosEditor(defaults)

    private class IosEditor(private val defaults: NSUserDefaults) : GamePreferences.Editor {
        override fun putInt(key: String, value: Int) = apply { defaults.setInteger(value.toLong(), forKey = key) }
        override fun putLong(key: String, value: Long) = apply { defaults.setInteger(value, forKey = key) }
        override fun putFloat(key: String, value: Float) = apply { defaults.setFloat(value, forKey = key) }
        override fun putBoolean(key: String, value: Boolean) = apply { defaults.setBool(value, forKey = key) }
        override fun putString(key: String, value: String) = apply { defaults.setObject(value, forKey = key) }
        override fun apply() { defaults.synchronize() }
    }
}
