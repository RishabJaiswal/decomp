package com.decomp.comp.decomp.utils

import android.graphics.Bitmap
import com.decomp.comp.decomp.ImageLruCache

object ThumbnailCache {
    private val thumbnails = HashMap<String, Bitmap>()

    //lru cache
    val lruCache by lazy {
        val maxSize = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        ImageLruCache(maxSize / 8)
    }

    fun save(filePath: String, thumbnailBitmap: Bitmap) {
        thumbnails[filePath] = thumbnailBitmap
    }

    fun get(filePath: String): Bitmap? = thumbnails[filePath]
}