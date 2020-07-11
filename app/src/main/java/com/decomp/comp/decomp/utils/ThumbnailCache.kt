package com.decomp.comp.decomp.utils

import android.graphics.Bitmap

object ThumbnailCache {
    private val thumbnails = HashMap<String, Bitmap>()

    fun save(filePath: String, thumbnailBitmap: Bitmap) {
        thumbnails[filePath] = thumbnailBitmap
    }

    fun get(filePath: String): Bitmap? = thumbnails[filePath]
}