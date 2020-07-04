package com.decomp.comp.decomp.utils.extensions

import android.content.Context
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat

fun Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.getColorStateList(context: Context): ColorStateList? {
    return ContextCompat.getColorStateList(context, this)
}