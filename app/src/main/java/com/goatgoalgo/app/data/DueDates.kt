package com.goatgoalgo.app.data

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

object DueDates {
    private val iso: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    fun normalize(value: String?): String? {
        val trimmed = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        require(isValid(trimmed)) { "截止日期无效" }
        return trimmed
    }

    fun isValid(value: String): Boolean =
        runCatching { LocalDate.parse(value, iso) }.isSuccess

    fun todayIso(today: LocalDate = LocalDate.now()): String = today.format(iso)

    fun format(value: String, today: LocalDate = LocalDate.now()): String {
        val date = LocalDate.parse(value, iso)
        val pattern = if (date.year == today.year) "M月d日" else "yyyy年M月d日"
        return date.format(DateTimeFormatter.ofPattern(pattern, Locale.CHINA))
    }

    fun isBeforeToday(value: String, today: LocalDate = LocalDate.now()): Boolean =
        LocalDate.parse(value, iso).isBefore(today)

    // Material3 DatePicker returns UTC midnight for the chosen calendar date.
    fun fromPickerUtcMillis(millis: Long): String =
        Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate().format(iso)

    fun toPickerUtcMillis(value: String): Long =
        LocalDate.parse(value, iso).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
}
