package com.decomp.comp.decomp.utils.extensions

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import androidx.core.content.ContextCompat

fun Int.getColor(context: Context): Int {
    return ContextCompat.getColor(context, this)
}

fun Int.getColorStateList(context: Context): ColorStateList? {
    return ContextCompat.getColorStateList(context, this)
}

fun Float.dpToPixels(context: Context): Float {
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
}