package com.example.myapp.game

import android.content.SharedPreferences

internal class RunPersistence(
    private val prefs: SharedPreferences
) {

    fun saveSnapshot(snapshot: RunSnapshot) {
        prefs.edit()
            .putInt("save_schema_version", snapshot.version)
            .putString("save_snapshot_v2", snapshot.toJsonString())
            .putBoolean("has_save", true)
            .apply()
    }

    fun loadSnapshot(): RunSnapshot? {
        val snapshotJson = prefs.getString("save_snapshot_v2", null) ?: return null
        return runCatching { RunSnapshot.fromJsonString(snapshotJson) }.getOrNull()
    }

    fun clearSnapshot() {
        prefs.edit()
            .remove("save_snapshot_v2")
            .remove("save_schema_version")
            .apply()
    }
}
