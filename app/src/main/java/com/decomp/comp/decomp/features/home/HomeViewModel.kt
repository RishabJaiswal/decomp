package com.decomp.comp.decomp.features.home

import androidx.lifecycle.ViewModel
import com.decomp.comp.decomp.R

class HomeViewModel : ViewModel() {
    fun getTasks(): List<Task> {
        return listOf(
                //compress image
                Task(R.string.task_title_compress_images,
                        R.string.task_details_compress_images,
                        R.drawable.art_task_compress_image,
                        R.color.blue1000,
                        TaskType.COMPRESS_IMAGE),

                //compress video
                Task(R.string.task_title_compress_videos,
                        R.string.task_details_compress_videos,
                        R.drawable.art_task_compress_video,
                        R.color.blue900,
                        TaskType.COMPRESS_VIDEO),

                //record screen
                Task(R.string.task_title_record_screen,
                        R.string.task_details_record_screen,
                        R.drawable.art_task_record_screen,
                        R.color.blue800,
                        TaskType.RECORD_SCREEN),

                //scan documents
                Task(R.string.task_title_scan_document,
                        R.string.task_details_scan_document,
                        R.drawable.art_task_record_screen,
                        R.color.blue800,
                        TaskType.SCAN_DOC)
        )
    }
}