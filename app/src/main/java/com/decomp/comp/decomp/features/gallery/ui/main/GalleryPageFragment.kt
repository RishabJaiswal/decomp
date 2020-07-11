package com.decomp.comp.decomp.features.gallery.ui.main

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.GalleryFilesAdapter
import com.decomp.comp.decomp.utils.extensions.dpToPixels
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File

/**
 * A placeholder fragment containing a simple view.
 */
class GalleryPageFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private var pagePosition: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pagePosition = arguments?.getInt(ARG_PAGE_POSITION) ?: 0
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_gallery, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        activity?.let { _activity ->
            galleryViewModel = ViewModelProvider(_activity).get(GalleryViewModel::class.java)
            setGalleryList()
        }
    }

    private fun setGalleryList() {
        context?.let { _context ->
            val thumbnailSpacing = 16.toFloat().dpToPixels(_context).toInt()
            val files = File(galleryViewModel.getPageFolderPath(pagePosition))
                    .listFiles()?.toMutableList() ?: emptyList<File>()
            //setting adapter
            rv_files.adapter = GalleryFilesAdapter(
                    files,
                    getThumbnailWidthInPx(_context),
                    thumbnailSpacing,
                    galleryViewModel.getTaskType(pagePosition)
            )
        }
    }

    private fun getThumbnailWidthInPx(context: Context): Int {
        val screenWidth = context.resources.displayMetrics.widthPixels
        //return thumbnail size
        return (screenWidth / 3).toInt()
    }

    companion object {
        private const val ARG_PAGE_POSITION = "page_number"

        @JvmStatic
        fun newInstance(sectionNumber: Int): GalleryPageFragment {
            return GalleryPageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PAGE_POSITION, sectionNumber)
                }
            }
        }
    }
}