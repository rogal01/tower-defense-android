package com.example.myapp.game

/**
 * Platform-agnostic key-value storage — replaces Android SharedPreferences.
 * Implemented differently on Android (SharedPreferences) and iOS (NSUserDefaults).
 */
interface GamePreferences {
    fun getInt(key: String, default: Int): Int
    fun getLong(key: String, default: Long): Long
    fun getFloat(key: String, default: Float): Float
    fun getBoolean(key: String, default: Boolean): Boolean
    fun getString(key: String, default: String): String

    fun edit(): Editor

    interface Editor {
        fun putInt(key: String, value: Int): Editor
        fun putLong(key: String, value: Long): Editor
        fun putFloat(key: String, value: Float): Editor
        fun putBoolean(key: String, value: Boolean): Editor
        fun putString(key: String, value: String): Editor
        fun apply()
    }
}
