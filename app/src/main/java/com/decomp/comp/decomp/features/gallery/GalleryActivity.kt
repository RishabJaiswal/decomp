package com.decomp.comp.decomp.features.gallery

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.BaseActivity
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
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_gallery.*
import kotlinx.android.synthetic.main.share_files_bar.*
import java.util.*

class GalleryActivity : BaseActivity(), SelectionCountListener {

    private lateinit var interstitialAd: InterstitialAd
    private lateinit var adRequest: AdRequest
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
        createGalleryPagesModels()
        observeUserSelection()
        setGalleryPages()
        initializeAd()

        //share bar bottomsheet
        shareBottomSheet.apply {
            peekHeight = 0
            addBottomSheetCallback(shareBottomsheetCallback)
        }

        //listening for all files selection changes
        btn_share_files.setOnClickListener {
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

        btn_delete_files.setOnClickListener {
            showAlertDialog(
                    R.string.deleteImages,
                    R.string.deleteImages,
                    android.R.string.ok,
                    onPositiveAction = {
                        viewModel.deleteUserSelectedFiles()
                    })
        }

        //clicking on select all
        cb_select_all.setOnClickListener {
            viewModel.selectAllFilesFor(
                    if (cb_select_all.isChecked)
                        viewModel.getTaskType(vp_gallery.currentItem)
                    else
                        null
            )
        }
    }

    fun initializeAd() {
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
                text = "$selectedFilesCount selected"
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

    companion object {
        @JvmStatic
        fun getIntent(context: Context): Intent {
            return Intent(context, GalleryActivity::class.java)
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