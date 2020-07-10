package com.decomp.comp.decomp.features.record_screen

import android.R
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
import android.os.Environment
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.decomp.comp.decomp.application.KEY_RESULT_SCREEN_CAST
import com.decomp.comp.decomp.application.PreferenceKeys
import com.decomp.comp.decomp.utils.PreferenceHelper
import com.decomp.comp.decomp.utils.extensions.createNotificationChannel
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RecordScreen : Service() {

    var DISPLAY_HEIGHT: Int = 0
    var DISPLAY_WIDTH: Int = 0
    var screenDensity: Int = 0
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
                0, notificationIntent, 0)

        val notification = NotificationCompat.Builder(this, notificationChannelID)
                .setContentTitle("DeComp is Recording")
                .setContentText("DeComp is helping you record the screen")
                .setSmallIcon(R.drawable.ic_popup_sync)
                .setContentIntent(pendingIntent)
                .addAction(R.drawable.arrow_up_float, "Stop recording", pendingIntent)
                .build()
        startForeground(1, notification)
    }

    //STEP - 1 screen recording
    private fun prepareScreenRecording(intent: Intent) {
        val resultCode = intent.getIntExtra(KEY_RESULT_SCREEN_CAST, Activity.RESULT_OK)
        initMediaRecorder()
        setupMediaProjection(resultCode, intent)
        startScreenRecording()
    }

    //STEP - 2
    private fun initMediaRecorder() {
        try {
            mediaRecorder = MediaRecorder()
            resources.displayMetrics.apply {
                DISPLAY_HEIGHT = heightPixels
                DISPLAY_WIDTH = widthPixels
                screenDensity = densityDpi
            }
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setOutputFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/video.mp4")
                setVideoSize(DISPLAY_WIDTH, DISPLAY_HEIGHT)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setVideoEncodingBitRate(512 * 1000)
                setVideoFrameRate(50)
                prepare()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
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
        mediaProjection?.unregisterCallback(mediaProjectionCallback);
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
    }
}
