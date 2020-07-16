package com.decomp.comp.decomp.features.home

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class Task(
        @StringRes var title: Int,
        @StringRes var details: Int,
        @DrawableRes var art: Int,
        @ColorRes var color: Int,
        var taskType: TaskType,
        var isFeatureReady: Boolean = false
)