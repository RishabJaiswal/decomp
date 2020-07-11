package com.decomp.comp.decomp.features.gallery.ui.main

import androidx.lifecycle.ViewModel
import com.decomp.comp.decomp.models.GalleryPage

class GalleryViewModel : ViewModel() {
    lateinit var galleryPageModels: List<GalleryPage>

    fun getPageTitle(position: Int): Int {
        return galleryPageModels[position].pageTitle
    }

    fun getTabText(position: Int): Int {
        return galleryPageModels[position].tabText
    }

    fun getPageFolderPath(position: Int): String {
        return galleryPageModels[position].folderPath
    }

    fun getTotalPages() = galleryPageModels.size
}