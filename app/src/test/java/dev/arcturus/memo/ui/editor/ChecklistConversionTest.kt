package dev.arcturus.memo.ui.editor

import org.junit.Assert.assertEquals
import org.junit.Test

class ChecklistConversionTest {
    @Test
    fun twoParagraphsConvertToTwoItemsAndRoundTrip() {
        val body = "Alpha paragraph one.\nBeta paragraph two."

        val lines = ChecklistConversion.bodyToLineTexts(body)

        assertEquals(listOf("Alpha paragraph one.", "Beta paragraph two."), lines)
        assertEquals(body, ChecklistConversion.itemsToBodyText(lines))
    }

    @Test
    fun emptyBodyConvertsToSingleEmptyItem() {
        assertEquals(listOf(""), ChecklistConversion.bodyToLineTexts(""))
    }

    @Test
    fun trailingNewlineRoundTripsThroughItems() {
        val body = "First line.\nSecond line.\n"

        val lines = ChecklistConversion.bodyToLineTexts(body)

        assertEquals(listOf("First line.", "Second line.", ""), lines)
        assertEquals(body, ChecklistConversion.itemsToBodyText(lines))
    }

    @Test
    fun enterAtEndSplitsItemIntoTextAndEmptyItem() {
        assertEquals(
            "Delta paragraph four." to "",
            ChecklistConversion.splitItem("Delta paragraph four.", "Delta paragraph four.".length),
        )
    }

    @Test
    fun enterInMiddleSplitsItemAtCursor() {
        assertEquals("abc" to "def", ChecklistConversion.splitItem("abcdef", 3))
    }
}
