package com.decomp.comp.decomp.utils.extensions

import java.text.SimpleDateFormat
import java.util.*

fun Date.getFormattedString(pattern: String): String {
    return SimpleDateFormat(pattern, Locale.getDefault())
            .format(this)
}