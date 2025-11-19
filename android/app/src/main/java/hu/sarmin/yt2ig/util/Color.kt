package hu.sarmin.yt2ig.util

import android.graphics.Bitmap
import androidx.palette.graphics.Palette

fun Bitmap.getGradientColors(): Pair<Int, Int> {
    val palette = Palette.Builder(this).generate()
    val dominant = palette.getDominantColor(0xFF000000.toInt())
    val darkMuted = palette.getDarkMutedColor(0xFFFFFFFF.toInt())

    return Pair(dominant, darkMuted)
}

fun Int.toHexRgb(): String {
    val r = (this shr 16) and 0xFF
    val g = (this shr 8) and 0xFF
    val b = this and 0xFF
    return String.format("#%02X%02X%02X", r, g, b)
}
