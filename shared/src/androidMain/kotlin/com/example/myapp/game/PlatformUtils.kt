package com.example.myapp.game

import java.util.Calendar

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun getDailySeed(): Long {
    val cal = Calendar.getInstance()
    return cal.get(Calendar.YEAR) * 10000L +
            (cal.get(Calendar.MONTH) + 1) * 100L +
            cal.get(Calendar.DAY_OF_MONTH)
}
