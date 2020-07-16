package com.decomp.comp.decomp.features.home

enum class TaskType(val value: String) {

    COMPRESS_IMAGE("compress_image"),
    COMPRESS_VIDEO("compress_video"),
    RECORD_SCREEN("record_screen"),
    SCAN_DOC("scan_documents"),
    UNKNOWN("unknown");

    companion object {
        private val values = values().associateBy(TaskType::value)
        fun from(value: String): TaskType = values[value] ?: UNKNOWN
    }
}