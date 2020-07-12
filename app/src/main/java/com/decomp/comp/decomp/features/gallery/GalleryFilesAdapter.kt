package com.decomp.comp.decomp.features.gallery

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.gallery.ui.main.GalleryViewModel
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.utils.ThumbnailCache
import com.decomp.comp.decomp.utils.extensions.visibleOrGone
import kotlinx.android.synthetic.main.item_gallery.view.*
import kotlinx.android.synthetic.main.item_gallery_video.view.*
import java.io.File

class GalleryFilesAdapter(
        private val viewModel: GalleryViewModel,
        private val files: List<File>,
        private val thumbnailSize: Int,
        private val thumbnailSpacing: Int,
        private val taskType: TaskType,
        val onFileClicked: (file: File) -> Unit) :
        RecyclerView.Adapter<GalleryFilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(getViewLayout(), parent, false)
        val layoutParams = RecyclerView.LayoutParams(thumbnailSize, thumbnailSize)

        view.layoutParams = layoutParams
        view.setPadding(
                thumbnailSpacing / 2,
                thumbnailSpacing / 2,
                thumbnailSpacing / 2,
                thumbnailSpacing / 2
        )
        return getViewHolder(view)
    }

    private fun getViewLayout(): Int {
        return when (taskType) {
            TaskType.RECORD_SCREEN -> R.layout.item_gallery_video
            TaskType.COMPRESS_VIDEO -> R.layout.item_gallery_video
            TaskType.COMPRESS_IMAGE -> R.layout.item_gallery
            else -> R.layout.item_gallery
        }
    }

    private fun getViewHolder(view: View): ViewHolder {
        return when (taskType) {
            TaskType.RECORD_SCREEN -> VideoViewHolder(view)
            TaskType.COMPRESS_VIDEO -> VideoViewHolder(view)
            TaskType.COMPRESS_IMAGE -> ImageViewHolder(view)
            else -> ViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(files[position])
    }

    override fun getItemCount(): Int {
        return files.size
    }

    fun selectAllFiles() {
        viewModel.setUserSelectedFiles(files)
        notifyDataSetChanged()
    }

    fun unSelectAllFiles() {
        viewModel.setUserSelectedFiles(emptyList())
        notifyDataSetChanged()
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        open fun bind(file: File) {}

        override fun onClick(v: View?) {
            if (viewModel.isUserSelectingFiles()) {
                val file = files[absoluteAdapterPosition]
                if (viewModel.hasUserSelected(file)) {
                    //adding user selected file
                    viewModel.removeSelectedFile(file)
                } else {
                    //removing user selected file
                    viewModel.addSelectedFile(file)
                }
                //checking the checkbox
                notifyItemChanged(absoluteAdapterPosition)
            } else {
                onFileClicked(files[absoluteAdapterPosition])
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (viewModel.isUserSelectingFiles().not()) {
                viewModel.addSelectedFile(files[absoluteAdapterPosition])
                viewModel.setUserSelectingFiles(true)
            }
            return true
        }
    }

    //Image
    inner class ImageViewHolder(itemView: View) : ViewHolder(itemView) {

        init {
            itemView.setOnClickListener(this@ImageViewHolder)
        }

        override fun bind(file: File) {
            var thumbnail = ThumbnailCache.get(file.absolutePath)
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(file.absolutePath),
                        thumbnailSize,
                        thumbnailSize
                )

                //thumbnail creation can fail
                if (thumbnail != null) {
                    ThumbnailCache.save(file.absolutePath, thumbnail)
                }
            }
            itemView.apply {
                imv_thumbnail.setImageBitmap(thumbnail)
                imv_thumbnail.clipToOutline = true
                cb_selected.visibleOrGone(viewModel.isUserSelectingFiles())
                cb_selected.isChecked = viewModel.hasUserSelected(file)
            }
        }
    }

    //video
    inner class VideoViewHolder(itemView: View) : ViewHolder(itemView) {

        override fun bind(file: File) {
            var thumbnail = ThumbnailCache.get(file.absolutePath)
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.createVideoThumbnail(
                        file.absolutePath,
                        MediaStore.Images.Thumbnails.MINI_KIND
                )

                //thumbnail creation can fail
                if (thumbnail != null) {
                    ThumbnailCache.save(file.absolutePath, thumbnail)
                }
            }
            itemView.imv_video_thumbnail.apply {
                setImageBitmap(thumbnail)
                clipToOutline = true
            }
        }
    }
}