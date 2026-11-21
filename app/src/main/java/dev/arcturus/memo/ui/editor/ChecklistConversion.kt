package dev.arcturus.memo.ui.editor

/**
 * Pure text <-> checklist conversions used by the note editor.
 *
 * Keep converts every line of a text note into its own list item (and back),
 * so no body text is ever discarded when toggling checkbox mode.
 */
object ChecklistConversion {
    fun bodyToLineTexts(body: String): List<String> = body.split("\n")

    fun itemsToBodyText(lines: List<String>): String = lines.joinToString("\n")

    /**
     * Splits a list item at [cursor] so pressing Enter continues the list with
     * the text after the caret in a fresh item.
     */
    fun splitItem(text: String, cursor: Int): Pair<String, String> {
        val safeCursor = cursor.coerceIn(0, text.length)
        return text.substring(0, safeCursor) to text.substring(safeCursor)
    }
}
