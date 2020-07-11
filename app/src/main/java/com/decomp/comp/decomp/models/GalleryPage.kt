package com.decomp.comp.decomp.models

import androidx.annotation.StringRes

data class GalleryPage(
        @StringRes var tabText: Int,
        @StringRes var pageTitle: Int,
        var folderPath: String
)