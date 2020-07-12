package com.decomp.comp.decomp.features.gallery.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decomp.comp.decomp.models.GalleryPage

class GalleryViewModel : ViewModel() {

    lateinit var galleryPageModels: List<GalleryPage>

    //defines if user is  selecting images for
    val isUserSelectingLD = MutableLiveData<Boolean>()

    fun getPageTitle(position: Int): Int {
        return galleryPageModels[position].pageTitle
    }

    fun getTabText(position: Int): Int {
        return galleryPageModels[position].tabText
    }

    fun getPageFolderPath(position: Int): String {
        return galleryPageModels[position].folderPath
    }

    fun getTaskType(position: Int) = galleryPageModels[position].taskType

    fun getTotalPages() = galleryPageModels.size

    //set/get user selection for files
    fun isUserSelectingFiles() = isUserSelectingLD.value ?: false
    fun setUserSelectingFiles(value: Boolean) {
        isUserSelectingLD.value = value
    }
}