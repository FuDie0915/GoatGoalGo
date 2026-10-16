package com.goatgoalgo.app.data

import androidx.room.TypeConverter

enum class TodoStatus(val wireName: String) {
    ACTIVE("ACTIVE"),
    COMPLETED("COMPLETED"),
    FAILED("FAILED"),
    ABANDONED("ABANDONED"),
    ;

    companion object {
        fun fromWire(value: String): TodoStatus =
            entries.firstOrNull { it.wireName == value }
                ?: throw IllegalArgumentException("Unknown todo status: $value")
    }
}

class TodoConverters {
    @TypeConverter
    fun fromStatus(status: TodoStatus): String = status.wireName

    @TypeConverter
    fun toStatus(value: String): TodoStatus = TodoStatus.fromWire(value)
}
