package com.decomp.comp.decomp.utils

import android.os.Environment
import java.io.File

object Directory {
    val publicDirName = "decomp"
    val recordedScreensDirName = "screens"

    fun getRecordedScreensDir() =
            Environment
                    .getExternalStorageDirectory()
                    .toString() + File.separator +
                    publicDirName + File.separator +
                    recordedScreensDirName

    fun getCompressedImagesDir() =
            Environment
                    .getExternalStorageDirectory()
                    .toString() + File.separator +
                    publicDirName

}