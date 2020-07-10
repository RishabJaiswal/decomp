package com.decomp.comp.decomp.features.record_screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_RESULT_SCREEN_CAST
import com.decomp.comp.decomp.application.PreferenceKeys
import com.decomp.comp.decomp.utils.PreferenceHelper
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_record_screen.*
import java.io.IOException

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RecordScreenActivity : AppCompatActivity(), View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    var DISPLAY_HEIGHT: Int = 0
    var DISPLAY_WIDTH: Int = 0
    var screenDensity: Int = 0
    private val REQUEST_PERMISSIONS = 0
    private val REQUEST_SCREEN_SHARE = 10
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaProjectionCallback: MediaProjectionCallback? = null
    private val projectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)
        btn_start_recording.setOnClickListener(this)
        changeRecordingState()
        listenSharedPreferenceListener()
    }

    override fun onDestroy() {
        PreferenceHelper.preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    private fun changeRecordingState() {
        if (isRecordingScreen()) {
            tv_lbl_record_screen.setText(R.string.lbl_recording_screen)
            tv_record_screen_details.setText(R.string.status_recording_details)
            btn_start_recording.setText(R.string.stop_recording)
        } else {
            tv_lbl_record_screen.setText(R.string.lbl_record_screen)
            tv_record_screen_details.setText(R.string.status_not_recording_details)
            btn_start_recording.setText(R.string.start_recording)
        }
    }

    //STEP - 1 screen recording
    private fun prepareScreenRecording() {
        initMediaRecorder()
        checkScreenCastToRecord()
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
    private fun checkScreenCastToRecord() {
        if (mediaProjection == null) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_SHARE);
        } else {
            startScreenRecording()
        }
    }

    //STEP - 4
    private fun startScreenRecording() {
        virtualDisplay = createVirtualDisplay()
        mediaRecorder?.start()
        PreferenceHelper.putValue(PreferenceKeys.IS_SCREEN_RECORDING, true)
        changeRecordingState()
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
            changeRecordingState()
        }
    }

    private fun createVirtualDisplay(): VirtualDisplay? {
        return mediaProjection?.createVirtualDisplay("MainActivity",
                DISPLAY_WIDTH, DISPLAY_HEIGHT, screenDensity,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mediaRecorder?.surface, null /*Callbacks*/, null /*Handler*/)
    }

    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        mediaProjectionCallback = MediaProjectionCallback()
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(mediaProjectionCallback, null)
    }

    private fun destroyMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback);
        mediaProjection?.stop()
        mediaProjection = null
        Log.i("screen recording", "MediaProjection Stopped")
    }

    private fun isRecordingScreen() =
            PreferenceHelper.getValue<Boolean>(PreferenceKeys.IS_SCREEN_RECORDING)

    /*requesting permissions*/
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_PERMISSIONS)
    }

    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    //permision was denied initially
                    (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.RECORD_AUDIO)) {
                showEnablePermissionSnack()
            } else {
                requestPermissions()
            }
        } else {
            //start recording the screen
            prepareScreenRecording()
        }
    }

    private fun showEnablePermissionSnack() {
        Snackbar.make(findViewById(android.R.id.content), "Please allow recording",
                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE") {
            requestPermissions()
        }.show()
    }

    private fun showOpenSettingSnack() {
        Snackbar.make(findViewById(android.R.id.content), "Allow access to screen & mic",
                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                View.OnClickListener {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.addCategory(Intent.CATEGORY_DEFAULT)
                    intent.data = Uri.parse("package:$packageName")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    startActivity(intent)
                }).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_SCREEN_SHARE) {
            Log.e("Screen recording", "Unknown request code: $requestCode")
            return
        }
        if (resultCode != Activity.RESULT_OK) {
            Toast.makeText(this, "Screen Cast Permission Denied", Toast.LENGTH_SHORT).show()
            return
        }

        //starting service
        data?.let {
            startService(Intent(this, RecordScreen::class.java)
                    .putExtras(data)
                    .putExtra(KEY_RESULT_SCREEN_CAST, resultCode))
        }
        //setupMediaProjection(resultCode, data ?: Intent())
        //startScreenRecording()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] +
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    prepareScreenRecording()
                } else {
                    showOpenSettingSnack()
                }
                return
            }
        }
    }

    fun listenSharedPreferenceListener() {
        PreferenceHelper.preferences.registerOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == PreferenceKeys.IS_SCREEN_RECORDING) {
            changeRecordingState()
        }
    }

    override fun onClick(v: View?) {
        if (isRecordingScreen()) {
            stopService(Intent(this, RecordScreen::class.java))
        } else {
            checkForPermissions()
        }
    }

    inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            stopScreenRecording()
        }
    }
}