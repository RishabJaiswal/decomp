package com.decomp.comp.decomp.features.gallery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryPagerAdapter
import com.google.android.material.tabs.TabLayout

class GalleryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        val sectionsPagerAdapter = GalleryPagerAdapter(this, supportFragmentManager)
        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        val tabs: TabLayout = findViewById(R.id.tabs)
        tabs.setupWithViewPager(viewPager)
    }

    companion object {
        fun getIntent(context: Context): Intent {
            return Intent(context, GalleryActivity::class.java)
        }
    }
}