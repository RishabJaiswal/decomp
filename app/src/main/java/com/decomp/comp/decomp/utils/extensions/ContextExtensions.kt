package com.decomp.comp.decomp.utils.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.FileProvider
import com.decomp.comp.decomp.BuildConfig
import java.io.File


fun Context.createNotificationChannel(channelId: String, channelName: String) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val serviceChannel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager: NotificationManager = this.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }
}

fun Context.getFileUri(file: File): Uri {
    return FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".file.provider", file)
}

fun Context.showShortToast(@StringRes msgRes: Int) {
    showShortToast(this.getString(msgRes))
}

fun Context.showShortToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

fun Context.showLongToast(@StringRes msgRes: Int) {
    showLongToast(this.getString(msgRes))
}

fun Context.showLongToast(msg: String) {
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}