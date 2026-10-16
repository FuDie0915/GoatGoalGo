package com.goatgoalgo.app.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DueDatesTest {
    private val today = LocalDate.of(2026, 10, 16)

    @Test
    fun formatsCurrentYearWithoutRepeatingYear() {
        assertEquals("10月20日", DueDates.format("2026-10-20", today))
        assertEquals("2027年1月2日", DueDates.format("2027-01-02", today))
    }

    @Test
    fun rejectsNonIsoDates() {
        assertFalse(DueDates.isValid("2026/10/16"))
        assertFalse(DueDates.isValid("2026-13-01"))
        assertTrue(DueDates.isValid("2026-10-16"))
    }

    @Test
    fun pickerMillisRoundTripsTheCalendarDate() {
        val iso = "2026-10-16"
        assertEquals(iso, DueDates.fromPickerUtcMillis(DueDates.toPickerUtcMillis(iso)))
        assertTrue(DueDates.isBeforeToday("2026-10-15", today))
        assertFalse(DueDates.isBeforeToday("2026-10-16", today))
    }
}
