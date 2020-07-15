package com.decomp.comp.decomp.features.gallery.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.models.GalleryPage
import java.io.File

class GalleryViewModel : ViewModel() {

    lateinit var galleryPageModels: List<GalleryPage>
    val userSelectedFiles = mutableListOf<File>()

    //defines if user is  selecting images for
    val isUserSelectingLD = MutableLiveData<Boolean>()
    val selectAllFilesLD = MutableLiveData<TaskType?>()
    val selectedFilesCountLD = MutableLiveData<Int>()

    fun getPageTitle(position: Int): Int {
        return galleryPageModels[position].pageTitle
    }

    fun getTabText(position: Int): Int {
        return galleryPageModels[position].tabText
    }

    fun getPageFolderPath(position: Int): String {
        return galleryPageModels[position].folderPath
    }

    /***returns files in the file directory corresponding to the current gallery page*/
    fun getGalleryPageFolderFiles(position: Int): List<File> {
        val files = File(getPageFolderPath(position)).listFiles() ?: emptyArray()
        return files.filter { file ->
            file.isFile
        }.toMutableList()
                .sortedByDescending { file ->
                    file.lastModified()
                }
    }

    fun getTaskType(position: Int) = galleryPageModels[position].taskType

    fun getTaskTypePosition(tasktype: TaskType): Int {
        galleryPageModels.forEachIndexed { index, galleryPage ->
            if (galleryPage.taskType == tasktype) {
                return index
            }
        }
        return 0
    }

    fun getTotalPages() = galleryPageModels.size

    fun addSelectedFile(file: File) {
        userSelectedFiles.add(file)
        updateSelectedFilesCount()
    }

    fun removeSelectedFile(file: File) {
        userSelectedFiles.remove(file)
        updateSelectedFilesCount()
    }

    /**deletes the user*/
    fun deleteUserSelectedFiles() {
        userSelectedFiles.forEach { file ->
            file.delete()
        }
        setUserSelectedFiles(emptyList())
    }

    fun hasUserSelected(file: File): Boolean {
        return userSelectedFiles.contains(file)
    }

    fun setUserSelectedFiles(files: List<File>) {
        userSelectedFiles.clear()
        if (files.isNotEmpty()) {
            userSelectedFiles.addAll(files)
        }
        updateSelectedFilesCount()
    }

    private fun updateSelectedFilesCount() {
        selectedFilesCountLD.value = userSelectedFiles.size
    }

    //set/get user selection for files
    fun isUserSelectingFiles() = isUserSelectingLD.value ?: false
    fun setUserSelectingFiles(isSelecting: Boolean) {
        isUserSelectingLD.value = isSelecting
        //clearing user selected files
        if (isSelecting.not()) {
            setUserSelectedFiles(emptyList())
        }
    }

    fun selectAllFilesFor(tasktype: TaskType?) {
        selectAllFilesLD.value = tasktype
    }

    fun getShareIntentType(tasktype: TaskType): String {
        return when (tasktype) {
            //images
            TaskType.COMPRESS_IMAGE -> "image/*"

            //videos
            TaskType.RECORD_SCREEN,
            TaskType.COMPRESS_VIDEO -> "video/*"

            else -> "*/*"
        }
    }
}