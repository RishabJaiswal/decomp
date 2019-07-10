package com.decomp.comp.decomp.utils.extensions

import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior

fun <T : View> BottomSheetBehavior<T>.toggle() {
    if (this.state == BottomSheetBehavior.STATE_EXPANDED) {
        this.state = BottomSheetBehavior.STATE_COLLAPSED
    } else if (this.state == BottomSheetBehavior.STATE_COLLAPSED) {
        this.state = BottomSheetBehavior.STATE_EXPANDED
    }
}