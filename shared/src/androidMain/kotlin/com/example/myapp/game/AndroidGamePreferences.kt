package com.example.myapp.game

import android.content.SharedPreferences

/** Android implementation of GamePreferences wrapping SharedPreferences */
class AndroidGamePreferences(private val prefs: SharedPreferences) : GamePreferences {

    override fun getInt(key: String, default: Int): Int = prefs.getInt(key, default)
    override fun getLong(key: String, default: Long): Long = prefs.getLong(key, default)
    override fun getFloat(key: String, default: Float): Float = prefs.getFloat(key, default)
    override fun getBoolean(key: String, default: Boolean): Boolean = prefs.getBoolean(key, default)
    override fun getString(key: String, default: String): String = prefs.getString(key, default) ?: default

    override fun edit(): GamePreferences.Editor = AndroidEditor(prefs.edit())

    private class AndroidEditor(private val editor: SharedPreferences.Editor) : GamePreferences.Editor {
        override fun putInt(key: String, value: Int) = apply { editor.putInt(key, value) }
        override fun putLong(key: String, value: Long) = apply { editor.putLong(key, value) }
        override fun putFloat(key: String, value: Float) = apply { editor.putFloat(key, value) }
        override fun putBoolean(key: String, value: Boolean) = apply { editor.putBoolean(key, value) }
        override fun putString(key: String, value: String) = apply { editor.putString(key, value) }
        override fun apply() { editor.apply() }
    }
}
