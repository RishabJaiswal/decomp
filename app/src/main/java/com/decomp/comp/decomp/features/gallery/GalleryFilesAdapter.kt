package com.decomp.comp.decomp.features.gallery

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.features.home.TaskType
import com.decomp.comp.decomp.utils.ThumbnailCache
import kotlinx.android.synthetic.main.item_gallery.view.*
import java.io.File

class GalleryFilesAdapter(
        private val list: List<File>,
        private val thumbnailSize: Int,
        private val thumbnailSpacing: Int,
        private val taskType: TaskType) :
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
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    open class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(file: File) {}
    }

    //Image
    inner class ImageViewHolder(itemView: View) : ViewHolder(itemView) {
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
            itemView.imv_thumbnail.apply {
                setImageBitmap(thumbnail)
                clipToOutline = true
            }
        }
    }

    //video
    inner class VideoViewHolder(itemView: View) : ViewHolder(itemView) {
        override fun bind(file: File) {

        }
    }
}