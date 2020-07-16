package com.decomp.comp.decomp.features.gallery

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.BaseActivity
import com.decomp.comp.decomp.application.KEY_TASK_TYPE
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryPagerAdapter
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryViewModel
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.models.GalleryPage
import com.decomp.comp.decomp.utils.Directory
import com.decomp.comp.decomp.utils.extensions.configureViewModel
import com.decomp.comp.decomp.utils.extensions.getFileUri
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.share_files_bar.*
import java.util.*

class GalleryActivity : BaseActivity(), SelectionCountListener, View.OnClickListener {

    private lateinit var interstitialAd: InterstitialAd
    private lateinit var adRequest: AdRequest
    private val REQUEST_PERMISSIONS = 11

    private val viewModel by lazy {
        configureViewModel<GalleryViewModel>()
    }

    private val shareBottomSheet by lazy {
        BottomSheetBehavior.from(share_bottom_sheet)
    }

    private val shareBottomsheetCallback = ShareBottomSheetCallback()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        checkForPermissions()
        observeUserSelection()
        initializeAd()

        //share bar bottomsheet
        shareBottomSheet.apply {
            peekHeight = 0
            addBottomSheetCallback(shareBottomsheetCallback)
        }

        //setting click listeners
        btn_share_files.setOnClickListener(this)
        cb_select_all.setOnClickListener(this)
        btn_delete_files.setOnClickListener(this)
    }

    private fun onPermissionsGranted() {
        setGalleryPages()
    }

    private fun initializeAd() {
        adRequest = AdRequest.Builder().build()
        interstitialAd = InterstitialAd(this)
        interstitialAd.adUnitId = getString(R.string.interstitial_adunit)
        interstitialAd.loadAd(adRequest)
    }

    override fun onDestroy() {
        shareBottomSheet.removeBottomSheetCallback(shareBottomsheetCallback)
        super.onDestroy()
    }

    override fun onBackPressed() {
        if (viewModel.isUserSelectingFiles()) {
            viewModel.setUserSelectingFiles(false)
        } else {
            if (interstitialAd.isLoaded)
                interstitialAd.show()
            super.onBackPressed()
        }
    }

    //observes if user is selecting files
    private fun observeUserSelection() {
        viewModel.isUserSelectingLD.observe(this, Observer { isUserSelecting ->
            if (isUserSelecting) {
                shareBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
            } else {
                shareBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            }
        })
    }

    private fun setGalleryPages() {

        //setting up models & titles
        createGalleryPagesModels()
        tv_page_title.setText(viewModel.getPageTitle(0))

        vp_gallery.adapter = GalleryPagerAdapter(this, viewModel, supportFragmentManager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(vp_gallery)

        //add page change listener
        vp_gallery.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tv_page_title.setText(viewModel.getPageTitle(position))
                viewModel.setUserSelectingFiles(false)
            }
        })

        //scrolling to a specific task page
        val taskType = TaskType.from(intent.getStringExtra(KEY_TASK_TYPE))
        val taskTypePosition = viewModel.getTaskTypePosition(taskType)
        vp_gallery.setCurrentItem(taskTypePosition, true)
    }

    //listening the number of files selected
    override fun onSelectedFilesChanged(
            selectedFilesCount: Int,
            areAllFilesSelected: Boolean,
            taskType: TaskType
    ) {
        if (viewModel.getTaskType(vp_gallery.currentItem) == taskType) {

            cb_select_all.apply {
                isChecked = areAllFilesSelected
                text = getString(R.string.files_selected, selectedFilesCount)
            }
        }
    }

    private fun createGalleryPagesModels() {


        val galleryPages = listOf(
                //images
                GalleryPage(
                        R.string.tab_text_images,
                        R.string.gallery_title_images,
                        Directory.getCompressedImagesDir(),
                        TaskType.COMPRESS_IMAGE
                ),

                GalleryPage(
                        R.string.tab_text_docs,
                        R.string.gallery_title_docs,
                        "",
                        TaskType.SCAN_DOC
                ),

                GalleryPage(
                        R.string.tab_text_screens,
                        R.string.gallery_title_screens,
                        Directory.getRecordedScreensDir(),
                        TaskType.RECORD_SCREEN
                ),

                GalleryPage(
                        R.string.tab_text_videos,
                        R.string.gallery_title_videos,
                        "",
                        TaskType.COMPRESS_VIDEO
                )
        )
        viewModel.galleryPageModels = galleryPages
    }

    /*requesting permissions*/
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
                arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ),
                REQUEST_PERMISSIONS)
    }

    private fun checkForPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            //permission was denied initially
            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showEnablePermissionSnack()
            } else {
                requestPermissions()
            }
        } else {
            onPermissionsGranted()
        }
    }

    private fun showEnablePermissionSnack() {
        Snackbar.make(findViewById(android.R.id.content), "Please allow file system access",
                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE") {
            requestPermissions()
        }.show()
    }

    private fun showOpenSettingSnack() {
        Snackbar.make(findViewById(android.R.id.content), "Allow access to file system",
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_PERMISSIONS -> {
                if (grantResults.isNotEmpty() &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    onPermissionsGranted()
                } else {
                    showOpenSettingSnack()
                }
                return
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {

            //listening for all files selection changes
            R.id.btn_share_files -> {
                val filesToShare = ArrayList<Uri>()
                for (file in viewModel.userSelectedFiles)
                    filesToShare.add(getFileUri(file))

                val intent = Intent().apply {
                    action = Intent.ACTION_SEND_MULTIPLE
                    type = viewModel.getShareIntentType(viewModel.getTaskType(vp_gallery.currentItem))
                    putParcelableArrayListExtra(Intent.EXTRA_STREAM, filesToShare)
                }
                startActivity(Intent.createChooser(intent, getString(R.string.share)))
            }

            //deleting files
            R.id.btn_delete_files -> {
                showAlertDialog(
                        R.string.are_you_sure,
                        R.string.deleteImages,
                        android.R.string.ok,
                        onPositiveAction = {
                            viewModel.deleteUserSelectedFiles()
                        })
            }

            //clicking on select all
            R.id.cb_select_all -> {
                viewModel.selectAllFilesFor(
                        if (cb_select_all.isChecked)
                            viewModel.getTaskType(vp_gallery.currentItem)
                        else
                            null
                )
            }
        }
    }

    companion object {
        @JvmStatic
        fun getIntent(context: Context, taskType: TaskType? = TaskType.UNKNOWN): Intent {
            return Intent(context, GalleryActivity::class.java).apply {
                if (taskType != null) {
                    putExtra(KEY_TASK_TYPE, taskType.value)
                }
            }
        }
    }

    inner class ShareBottomSheetCallback : BottomSheetBehavior.BottomSheetCallback() {

        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                viewModel.setUserSelectingFiles(false)
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {
        }
    }
}

interface SelectionCountListener {
    fun onSelectedFilesChanged(selectedFilesCount: Int, areAllFilesSelected: Boolean, taskType: TaskType)
}