package com.decomp.comp.decomp.utils.extensions

import android.content.Context
import androidx.core.content.ContextCompat

fun Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context, this)
}