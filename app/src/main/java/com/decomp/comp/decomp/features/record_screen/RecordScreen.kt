package com.decomp.comp.decomp.features.record_screen

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.crashlytics.android.Crashlytics
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_RECORD_WITH_AUDIO
import com.decomp.comp.decomp.application.KEY_RESULT_SCREEN_CAST
import com.decomp.comp.decomp.application.KEY_STOP_RECORDING
import com.decomp.comp.decomp.application.PreferenceKeys
import com.decomp.comp.decomp.utils.Directory
import com.decomp.comp.decomp.utils.PreferenceHelper
import com.decomp.comp.decomp.utils.extensions.createNotificationChannel
import com.decomp.comp.decomp.utils.extensions.getFormattedString
import com.decomp.comp.decomp.utils.extensions.showLongToast
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

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            prepareScreenRecording(intent)
        }
        createNotification()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        createNotificationChannel(RecordScreen.notificationChannelID, RecordScreen.notificationChannelName)
        val notificationIntent = Intent(this, RecordScreenActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, notificationChannelID)
                .setContentTitle(applicationContext.getString(R.string.recording_notif_title))
                .setContentText(applicationContext.getString(R.string.recording_notif_title))
                .setSmallIcon(android.R.drawable.ic_popup_sync)
                .setContentIntent(pendingIntent)
                .addAction(android.R.drawable.arrow_up_float, "Stop recording", pendingIntent)
                .build()
        startForeground(1, notification)
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
            Toast.makeText(
                    applicationContext,
                    R.string.error_directory_creation,
                    Toast.LENGTH_SHORT
            ).show()
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
                setOutputFile(videoFilePath)
                setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                //audio encoder
                if (isAudioRecordingEnabled) {
                    setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                }
                setVideoEncodingBitRate(1024 * 1000)
                setVideoFrameRate(50)
                prepare()
            }
        } catch (e: IOException) {
            e.printStackTrace()
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
        mediaProjectionCallback = ScreenProjectionCallback()
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(mediaProjectionCallback, null)
    }

    //STEP - 4
    private fun startScreenRecording() {
        virtualDisplay = createVirtualDisplay()
        mediaRecorder?.start()
        PreferenceHelper.putValue(PreferenceKeys.IS_SCREEN_RECORDING, true)
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
                } catch (error: RuntimeException) {

                }
            }
            destroyMediaProjection()
            PreferenceHelper.putValue(PreferenceKeys.IS_SCREEN_RECORDING, false)

        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("MainActivity",
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
