package com.decomp.comp.decomp.features.gallery

import android.graphics.Bitmap
import android.os.AsyncTask
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.AsyncDrawable
import com.decomp.comp.decomp.ImgLoadAsynTask
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
        private val thumbnailSize: Int,
        private val thumbnailSpacing: Int,
        private val taskType: TaskType,
        val onFileClicked: (file: File) -> Unit) : ListAdapter<File, GalleryFilesAdapter.ViewHolder>(FilesDiffCallback()) {

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
        holder.bind(getItem(position))
    }

    fun selectAllFiles() {
        viewModel.setUserSelectedFiles(currentList)
        notifyDataSetChanged()
    }

    fun unSelectAllFiles() {
        viewModel.setUserSelectedFiles(emptyList())
        notifyDataSetChanged()
    }

    private fun loadBitmap(imageView: ImageView, file: File) {
        var bitmap: Bitmap?
        synchronized(ThumbnailCache.lruCache) {
            bitmap = ThumbnailCache.lruCache.get(file.absolutePath)
        }
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap)
            return
        }
        val context = imageView.context
        val task = ImgLoadAsynTask(
                imageView.context,
                imageView, thumbnailSize,
                taskType,
                ThumbnailCache.lruCache)
        val asyncDrawable = AsyncDrawable(context.resources, null, task)
        imageView.setImageDrawable(asyncDrawable)
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, file)
    }

    open inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener, View.OnLongClickListener {
        init {
            itemView.setOnClickListener(this)
            itemView.setOnLongClickListener(this)
        }

        open fun bind(file: File) {}

        override fun onClick(v: View?) {
            val file = getItem(absoluteAdapterPosition)
            if (viewModel.isUserSelectingFiles()) {
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
                onFileClicked(file)
            }
        }

        override fun onLongClick(v: View?): Boolean {
            if (viewModel.isUserSelectingFiles().not()) {
                viewModel.addSelectedFile(getItem(absoluteAdapterPosition))
                viewModel.setUserSelectingFiles(true)
            }
            return true
        }
    }

    //Image
    inner class ImageViewHolder(itemView: View) : ViewHolder(itemView) {

        init {
            itemView.imv_thumbnail.clipToOutline = true
        }

        override fun bind(file: File) {
            itemView.apply {
                loadBitmap(imv_thumbnail, file)
                cb_selected.visibleOrGone(viewModel.isUserSelectingFiles())
                cb_selected.isChecked = viewModel.hasUserSelected(file)
            }
        }
    }

    //video
    inner class VideoViewHolder(itemView: View) : ViewHolder(itemView) {

        init {
            itemView.imv_video_thumbnail.clipToOutline = true
        }

        override fun bind(file: File) {
            itemView.apply {
                loadBitmap(imv_video_thumbnail, file)
                cb_video_selected.visibleOrGone(viewModel.isUserSelectingFiles())
                cb_video_selected.isChecked = viewModel.hasUserSelected(file)
            }
        }
    }

    private class FilesDiffCallback : DiffUtil.ItemCallback<File>() {

        override fun areItemsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: File, newItem: File): Boolean {
            return oldItem.absolutePath == newItem.absolutePath
        }

    }
}