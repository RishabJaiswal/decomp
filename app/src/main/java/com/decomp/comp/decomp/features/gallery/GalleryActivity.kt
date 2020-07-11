package com.decomp.comp.decomp.features.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryPagerAdapter
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_gallery.*

class GalleryActivity : AppCompatActivity() {

    private val GALLERY_TITLES = arrayOf(
            R.string.gallery_title_images,
            R.string.gallery_title_docs,
            R.string.gallery_title_screens,
            R.string.gallery_title_videos
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        setGalleryPages()
    }

    private fun setGalleryPages() {
        vp_gallery.adapter = GalleryPagerAdapter(this, supportFragmentManager)
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(vp_gallery)

        //add page change listener
        vp_gallery.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            override fun onPageSelected(position: Int) {
                tv_page_title.setText(GALLERY_TITLES[position])
            }
        })

    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, GalleryActivity::class.java)
        }
    }
}