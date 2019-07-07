package com.decomp.comp.decomp.utils

import android.view.View
import androidx.annotation.ColorInt
import androidx.core.graphics.drawable.DrawableCompat

fun View.gone() {
    this.visibility = View.GONE
}

fun View.visible() {
    this.visibility = View.VISIBLE
}

fun View.invisible() {
    this.visibility = View.INVISIBLE
}

fun View.isVisible(): Boolean {
    return this.visibility == View.VISIBLE
}

fun View.isGone(): Boolean {
    return this.visibility == View.GONE
}

fun View.isInvisible(): Boolean {
    return this.visibility == View.INVISIBLE
}

fun View.visibleOrGone(isViewVisible: Boolean) {
    if (isViewVisible) {
        this.visible()
    } else {
        this.gone()
    }
}

fun View.setBackgroundTint(@ColorInt color: Int) {
    val bgDrawable = this.background.mutate()
    DrawableCompat.setTint(bgDrawable, color)
    this.background = bgDrawable
}