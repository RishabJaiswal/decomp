package com.decomp.comp.decomp.features.gallery.ui.main

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class GalleryPagerAdapter(
        private val context: Context,
        private val viewModel: GalleryViewModel,
        fm: FragmentManager)
    : FragmentPagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        return GalleryPageFragment.newInstance(position)
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.resources.getString(viewModel.getTabText(position))
    }

    override fun getCount(): Int {
        return viewModel.getTotalPages()
    }
}