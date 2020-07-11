package com.decomp.comp.decomp.models

import androidx.annotation.StringRes
import com.decomp.comp.decomp.features.home.TaskType

data class GalleryPage(
        @StringRes var tabText: Int,
        @StringRes var pageTitle: Int,
        var folderPath: String,
        var taskType: TaskType
)