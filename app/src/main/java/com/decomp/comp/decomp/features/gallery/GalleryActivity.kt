package com.decomp.comp.decomp.features.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryPagerAdapter
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryViewModel
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.models.GalleryPage
import com.decomp.comp.decomp.utils.Directory
import com.decomp.comp.decomp.utils.extensions.configureViewModel
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {

    private val viewModel by lazy {
        configureViewModel<GalleryViewModel>()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        createGalleryPagesModels()
        setGalleryPages()
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
            }
        })

    }

    private fun createGalleryPagesModels() {
        val compressedImagesDir = getSharedPreferences("dir", Context.MODE_PRIVATE)
                ?.getString("dir", null) ?: ""

        val recordedScreensDir = Directory.getRecordedScreensDir()

        val galleryPages = listOf(
                //images
                GalleryPage(
                        R.string.tab_text_images,
                        R.string.gallery_title_images,
                        compressedImagesDir,
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
                        recordedScreensDir,
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
        fun getIntent(context: Context): Intent {
            return Intent(context, GalleryActivity::class.java)
        }
    }
}