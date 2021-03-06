package com.decomp.comp.decomp.features.record_screen

import android.app.Activity
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.crashlytics.android.Crashlytics
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.*
import com.decomp.comp.decomp.features.gallery.GalleryActivity
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.utils.Directory
import com.decomp.comp.decomp.utils.PreferenceHelper
import com.decomp.comp.decomp.utils.extensions.createNotificationChannel
import com.decomp.comp.decomp.utils.extensions.getFormattedString
import com.decomp.comp.decomp.utils.extensions.showLongToast
import com.decomp.comp.decomp.utils.extensions.showShortToast
import java.io.File
import java.io.IOException
import java.util.*


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RecordScreen : Service() {

    var DISPLAY_HEIGHT: Int = 0
    var DISPLAY_WIDTH: Int = 0
    var screenDensity: Int = 0
    private var isAudioRecordingEnabled = false
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjectionCallback: ScreenProjectionCallback? = null
    private val projectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }
    private val notificationManager by lazy {
        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            createNotification()
            prepareScreenRecording(intent)
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        createNotificationChannel(RecordScreen.notificationChannelID, RecordScreen.notificationChannelName)
        val notificationIntent = Intent(this, RecordScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, notificationChannelID)
                .setContentTitle(applicationContext.getString(R.string.recording_notif_title))
                .setContentText(applicationContext.getString(R.string.recording_notif_msg))
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.arrow_up_float, "Stop recording", pendingIntent)
                .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
        } else {
            startForeground(1, notification)
        }
    }

    // creates a intent for RecordScreenActivity and telling it to stop recording
    private fun getStopRecordingIntent(): PendingIntent {
        val notificationIntent = Intent(this, RecordScreenActivity::class.java).apply {
            putExtra(KEY_STOP_RECORDING, true)
        }
        return PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    //STEP - 1 screen recording
    private fun prepareScreenRecording(intent: Intent) {
        val videoFilePath = getFilePath()
        if (videoFilePath.isNotEmpty()) {
            val resultCode = intent.getIntExtra(KEY_RESULT_SCREEN_CAST, Activity.RESULT_OK)
            isAudioRecordingEnabled = intent.getBooleanExtra(KEY_RECORD_WITH_AUDIO, false)
            initMediaRecorder(videoFilePath)
            setupMediaProjection(resultCode, intent)
            startScreenRecording()
        } else {
            applicationContext.showShortToast(R.string.error_directory_creation)
            Crashlytics.logException(Exception("unable to create recorded screens directory"))
            stopSelf()
        }
    }

    //STEP - 2
    private fun initMediaRecorder(videoFilePath: String) {
        try {
            mediaRecorder = MediaRecorder()
            resources.displayMetrics.apply {
                DISPLAY_HEIGHT = heightPixels
                DISPLAY_WIDTH = widthPixels
                screenDensity = densityDpi
            }
            mediaRecorder?.apply {
                //audio source
                if (isAudioRecordingEnabled) {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                }
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                //audio encoder
                if (isAudioRecordingEnabled) {
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                setVideoFrameRate(30)
                setOutputFile(videoFilePath)
                setVideoEncodingBitRate(10 * 1024 * 1000)
                prepare()
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Crashlytics.logException(e)
        }
    }

    //returns file for recorded screen
    private fun getFilePath(): String {
        val folder = File(Directory.getRecordedScreensDir())
        if (folder.exists().not()) {
            val isSuccess = folder.mkdirs()
            if (!isSuccess) {
                return ""
            }
        }
        return "${folder.absolutePath}${File.separator}${getUniqueFileName()}"
    }

    private fun getUniqueFileName(): String {
        val dateTime = Date().getFormattedString("dd-MMM-yyy_HH:mm:ss")
        return "screen_$dateTime.mp4"
    }

    //STEP - 3
    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjectionCallback = ScreenProjectionCallback()
        mediaProjection?.registerCallback(mediaProjectionCallback, null)
    }

    //STEP - 4
    private fun startScreenRecording() {
        try {
            virtualDisplay = createVirtualDisplay()
            mediaRecorder?.start()
            PreferenceHelper.putValue(PreferenceKeys.IS_SCREEN_RECORDING, true)
        } catch (exception: java.lang.Exception) {
            Crashlytics.logException(exception)
            applicationContext.showShortToast(R.string.failed_to_record)
        }
    }

    //STEP - 5
    private fun stopScreenRecording() {
        if (isRecordingScreen()) {
            mediaRecorder?.apply {
                try {
                    stop()
                    reset()
                    release()
                    virtualDisplay?.release()
                    applicationContext.showLongToast(R.string.screen_saved)
                    showRecordingSavedNotification()
                } catch (error: RuntimeException) {

                }
            }
            destroyMediaProjection()
            PreferenceHelper.putValue(PreferenceKeys.IS_SCREEN_RECORDING, false)

        }
    }

    private fun showRecordingSavedNotification() {
        createNotificationChannel(RecordScreen.notificationChannelID, RecordScreen.notificationChannelName)
        val notificationIntent = GalleryActivity.getIntent(this, TaskType.RECORD_SCREEN)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, notificationChannelID)
                .setContentTitle(applicationContext.getString(R.string.recording_saved_title))
                .setContentText(applicationContext.getString(R.string.recording_saved_msg))
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .build()

        notificationManager.notify(Notifications.ID_RECORDING_SAVED, notification)
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("RecordScreen",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface, null /*Callbacks*/, null /*Handler*/)
    }

    private fun destroyMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback)
        mediaProjection?.stop()
        mediaProjection = null
        Log.i("screen recording", "MediaProjection Stopped")
    }

    private fun isRecordingScreen() =
            PreferenceHelper.getValue<Boolean>(PreferenceKeys.IS_SCREEN_RECORDING)

    override fun onDestroy() {
        stopScreenRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    inner class ScreenProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            stopScreenRecording()
        }
    }

    companion object {
        val notificationChannelName = "Record screen"
        val notificationChannelID = "record_screen"
        val notificationID = "662"

        fun getIntent(context: Context): Intent {
            return Intent(context, RecordScreen::class.java)
        }
    }
}
