package com.decomp.comp.decomp.features.gallery.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.decomp.comp.decomp.BuildConfig
import com.decomp.comp.decomp.ImagePager
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.GalleryFilesAdapter
import com.decomp.comp.decomp.features.gallery.SelectionCountListener
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.utils.extensions.dpToPixels
import com.decomp.comp.decomp.utils.extensions.visibleOrGone
import kotlinx.android.synthetic.main.fragment_gallery.*
import java.io.File


/**
 * A placeholder fragment containing a simple view.
 */
class GalleryPageFragment : Fragment() {

    private lateinit var galleryViewModel: GalleryViewModel
    private lateinit var galleryFilesAdapter: GalleryFilesAdapter
    private var pagePosition: Int = 0
    private lateinit var fileSelectionCountListener: SelectionCountListener

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
            fileSelectionCountListener = _activity as SelectionCountListener
            setGalleryList()
            observeUserSelection()
            observeSelectedFilesCount()
            observeAllFilesSelection()
        }
    }

    //observes if user is selecting files
    private fun observeUserSelection() {
        galleryViewModel.isUserSelectingLD.observe(viewLifecycleOwner, Observer { isUserSelecting ->
            galleryFilesAdapter.notifyDataSetChanged()
        })
    }

    //observing change in selected files count
    private fun observeSelectedFilesCount() {
        galleryViewModel.selectedFilesCountLD.observe(viewLifecycleOwner, Observer { filesCount ->
            val areAllFilesSelected = filesCount == galleryFilesAdapter.files.size
            fileSelectionCountListener.onSelectedFilesChanged(
                    filesCount, areAllFilesSelected, galleryViewModel.getTaskType(pagePosition)
            )
        })
    }

    private fun observeAllFilesSelection() {
        galleryViewModel.selectAllFilesLD.observe(viewLifecycleOwner, Observer { currentPageTaskType ->
            val thisPageTaskType = galleryViewModel.getTaskType(pagePosition)
            if (thisPageTaskType == currentPageTaskType) {
                galleryFilesAdapter.selectAllFiles()
            } else if (currentPageTaskType == null) {
                galleryFilesAdapter.unSelectAllFiles()
            }
        })
    }

    private fun setGalleryList() {
        context?.let { _context ->
            val thumbnailSpacing = 16.toFloat().dpToPixels(_context).toInt()
            val files = File(galleryViewModel.getPageFolderPath(pagePosition))
                    .listFiles()?.toMutableList() ?: emptyList<File>()
            blank_slate_files.visibleOrGone(files.isEmpty())

            //setting adapter
            galleryFilesAdapter = GalleryFilesAdapter(
                    galleryViewModel,
                    files,
                    getThumbnailWidthInPx(_context),
                    thumbnailSpacing,
                    galleryViewModel.getTaskType(pagePosition),
                    this::onFileClicked
            )
            rv_files.adapter = galleryFilesAdapter
        }
    }

    private fun getThumbnailWidthInPx(context: Context): Int {
        val screenWidth = context.resources.displayMetrics.widthPixels
        //return thumbnail size
        return (screenWidth / 3).toInt()
    }

    private fun onFileClicked(file: File) {
        when (galleryViewModel.getTaskType(pagePosition)) {

            //image files
            TaskType.COMPRESS_IMAGE -> {
                val i = Intent(context, ImagePager::class.java)
                i.putExtra("filepath", file.absolutePath)
                startActivity(i)
            }

            //video file
            TaskType.COMPRESS_VIDEO,
            TaskType.RECORD_SCREEN -> openVideoPlayer(file)
        }
    }

    private fun openVideoPlayer(videoFile: File) {
        val fileUri = FileProvider.getUriForFile(
                context!!,
                "${BuildConfig.APPLICATION_ID}.file.provider",
                videoFile
        )
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(fileUri, "video/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) //DO NOT FORGET THIS EVER
        startActivity(intent)
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