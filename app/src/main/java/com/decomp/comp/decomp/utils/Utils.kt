package com.decomp.comp.decomp.utils

import android.util.Pair

object Utils {

    private var b = arrayOf("B", "KB", "MB", "GB", "TB")
    fun convertSize(mag: Float, unit: Int): Pair<Float, String> {
        return if (mag >= 1024) convertSize(mag / 1024, unit + 1) else Pair(mag, b[unit])
    }
}