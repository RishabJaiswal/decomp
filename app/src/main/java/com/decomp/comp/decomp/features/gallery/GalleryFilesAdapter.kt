package com.decomp.comp.decomp.features.gallery

import android.graphics.BitmapFactory
import android.media.ThumbnailUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.utils.ThumbnailCache
import kotlinx.android.synthetic.main.item_gallery.view.*
import java.io.File

class GalleryFilesAdapter(
        private val list: List<File>,
        private val thumbnailSize: Int,
        private val thumbnailSpacing: Int) :
        RecyclerView.Adapter<GalleryFilesAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gallery, parent, false)
        val layoutParams = RecyclerView.LayoutParams(thumbnailSize, thumbnailSize)

        view.layoutParams = layoutParams
        view.setPadding(
                thumbnailSpacing / 2,
                thumbnailSpacing / 2,
                thumbnailSpacing / 2,
                thumbnailSpacing / 2
        )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(file: File) {
            var thumbnail = ThumbnailCache.get(file.absolutePath)
            if (thumbnail == null) {
                thumbnail = ThumbnailUtils.extractThumbnail(
                        BitmapFactory.decodeFile(file.absolutePath),
                        200,
                        200
                )
                ThumbnailCache.save(file.absolutePath, thumbnail)
            }
            itemView.imv_thumbnail.apply {
                setImageBitmap(thumbnail)
                clipToOutline = true
            }
        }
    }
}