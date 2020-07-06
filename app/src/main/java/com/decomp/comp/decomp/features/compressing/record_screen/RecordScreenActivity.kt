package com.decomp.comp.decomp.features.compressing.record_screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_record_screen.*
import java.io.IOException


@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RecordScreenActivity : AppCompatActivity(), View.OnClickListener {

    private val DISPLAY_HEIGHT: Int = 1366
    private val DISPLAY_WIDTH: Int = 768
    private val REQUEST_PERMISSIONS = 0
    private val REQUEST_SCREEN_SHARE = 10
    private var mediaRecorder: MediaRecorder? = null
    private var mediaProjection: MediaProjection? = null
    private var mediaProjectionCallback: MediaProjectionCallback? = null
    private val projectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)
        btn_start_recording.setOnClickListener(this)
    }

    //STEP - 1 screen recording
    private fun prepareScreenRecording() {
        initMediaRecorder()
        startScreenRecording()
    }

    //STEP - 2
    private fun initMediaRecorder() {
        try {
            mediaRecorder = MediaRecorder()
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
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
    fun startScreenRecording() {
        if (mediaProjection == null) {
            startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_SHARE);
        } else {
            mediaRecorder?.start()
        }
    }

    private fun setupMediaProjection(resultCode: Int, data: Intent) {
        mediaProjectionCallback = MediaProjectionCallback()
        mediaProjection = projectionManager.getMediaProjection(resultCode, data)
        mediaProjection?.registerCallback(mediaProjectionCallback, null)
        mediaRecorder?.start()
    }

    private fun destroyMediaProjection() {
        mediaProjection?.unregisterCallback(mediaProjectionCallback);
        mediaProjection?.stop();
        mediaProjection = null;
        Log.i("screen recording", "MediaProjection Stopped");
    }

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
        setupMediaProjection(resultCode, data ?: Intent())
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

    override fun onClick(v: View?) {
        checkForPermissions()
    }

    inner class MediaProjectionCallback : MediaProjection.Callback() {
        override fun onStop() {
            super.onStop()
            destroyMediaProjection()
        }
    }
}