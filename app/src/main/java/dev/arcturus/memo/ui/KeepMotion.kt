package dev.arcturus.memo.ui

import androidx.compose.animation.core.Easing

/** The fragment motion path shipped in the reference APK's fragment_fast_out_extra_slow_in.xml. */
internal object KeepMotion {
    const val SCREEN_DURATION_MS = 300

    val fragmentFastOutExtraSlowIn = Easing { fraction ->
        val x = fraction.coerceIn(0f, 1f)
        val firstSegment = x <= 0.166666f
        val startX = if (firstSegment) 0f else 0.166666f
        val endX = if (firstSegment) 0.166666f else 1f
        val startY = if (firstSegment) 0f else 0.4f
        val endY = if (firstSegment) 0.4f else 1f
        val controlX1 = if (firstSegment) 0.05f else 0.208333f
        val controlX2 = if (firstSegment) 0.133333f else 0.25f
        val controlY1 = if (firstSegment) 0f else 0.82f
        val controlY2 = if (firstSegment) 0.06f else 1f

        var low = 0f
        var high = 1f
        repeat(16) {
            val middle = (low + high) * 0.5f
            val currentX = cubic(startX, controlX1, controlX2, endX, middle)
            if (currentX < x) low = middle else high = middle
        }
        cubic(startY, controlY1, controlY2, endY, (low + high) * 0.5f)
    }

    private fun cubic(start: Float, firstControl: Float, secondControl: Float, end: Float, t: Float): Float {
        val inverse = 1f - t
        return inverse * inverse * inverse * start +
            3f * inverse * inverse * t * firstControl +
            3f * inverse * t * t * secondControl +
            t * t * t * end
    }
}
