package com.decomp.comp.decomp.features.record_screen

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_RECORD_WITH_AUDIO
import com.decomp.comp.decomp.application.KEY_RESULT_SCREEN_CAST
import com.decomp.comp.decomp.application.KEY_STOP_RECORDING
import com.decomp.comp.decomp.application.PreferenceKeys
import com.decomp.comp.decomp.utils.PreferenceHelper
import com.decomp.comp.decomp.utils.extensions.visibleOrGone
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_record_screen.*

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RecordScreenActivity : AppCompatActivity(), View.OnClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private val REQUEST_PERMISSIONS = 0
    private val REQUEST_SCREEN_SHARE = 10
    private val projectionManager by lazy {
        getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_record_screen)

        //stopping screen recording if intent fired from service notification
        val shouldStopScreenRecording = intent.getBooleanExtra(KEY_STOP_RECORDING, false)
        if (shouldStopScreenRecording) {
            stopService(RecordScreen.getIntent(this))
        }
        btn_start_recording.setOnClickListener(this)
        changeRecordingState()
        listenSharedPreferenceListener()
    }

    override fun onDestroy() {
        PreferenceHelper.preferences.unregisterOnSharedPreferenceChangeListener(this)
        super.onDestroy()
    }

    //UI state management
    private fun changeRecordingState() {
        val isRecording = isRecordingScreen()
        anim_recording.visibleOrGone(isRecording)
        imv_art_record_screen.visibleOrGone(isRecording.not())
        cb_enable_audio.visibleOrGone(isRecording.not())
        if (isRecording) {
            tv_lbl_record_screen.setText(R.string.lbl_recording_screen)
            tv_record_screen_details.setText(R.string.status_recording_details)
            btn_start_recording.setText(R.string.stop_recording)
        } else {
            tv_lbl_record_screen.setText(R.string.lbl_record_screen)
            tv_record_screen_details.setText(R.string.status_not_recording_details)
            btn_start_recording.setText(R.string.start_recording)
        }
    }

    //checking permissions for screen cast
    private fun checkScreenCastToRecord() {
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_SHARE)
    }

    private fun isRecordingScreen() =
            PreferenceHelper.getValue<Boolean>(PreferenceKeys.IS_SCREEN_RECORDING)

    /*requesting permissions*/
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO
                ),
                REQUEST_PERMISSIONS)
    }

    private fun checkForPermissions() {
        if (
                ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED
        ) {

            if (
            //permision was denied initially
                    ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.RECORD_AUDIO)
            ) {
                showEnablePermissionSnack()
            } else {
                requestPermissions()
            }
        } else {
            //start recording the screen
            checkScreenCastToRecord()
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

        //starting service to record screen
        data?.let {
            startService(Intent(this, RecordScreen::class.java)
                    .putExtras(data)
                    .putExtra(KEY_RESULT_SCREEN_CAST, resultCode)
                    .putExtra(KEY_RECORD_WITH_AUDIO, cb_enable_audio.isChecked))
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() && grantResults[0] +
                        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    checkScreenCastToRecord()
                } else {
                    showOpenSettingSnack()
                }
                return
            }
        }
    }

    private fun listenSharedPreferenceListener() {
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
}