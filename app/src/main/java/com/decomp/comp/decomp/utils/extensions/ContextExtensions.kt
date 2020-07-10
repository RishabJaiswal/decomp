package com.decomp.comp.decomp.utils.extensions

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build


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