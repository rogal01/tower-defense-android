package com.example.myapp.game

/** Returns current time in milliseconds since epoch */
expect fun currentTimeMillis(): Long

/** Returns a seed based on the current day (YYYYMMDD as Long) for daily challenges */
expect fun getDailySeed(): Long
