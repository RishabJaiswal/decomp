package com.decomp.comp.decomp.application

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.decomp.comp.decomp.BuildConfig
import java.io.File

object ImageFileProvider {
    fun getImageUri(context: Context, filepath: String): Uri {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".file.provider",
                File(filepath))
    }

    fun getImageUri(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(context,
                BuildConfig.APPLICATION_ID + ".file.provider",
                File(file.path))
    }
}