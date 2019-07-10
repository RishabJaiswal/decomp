package com.decomp.comp.decomp.features.compressing

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.ImageFileProvider
import com.decomp.comp.decomp.utils.Utils
import com.decomp.comp.decomp.utils.extensions.getColor
import com.decomp.comp.decomp.utils.setBackgroundTint
import com.decomp.comp.decomp.utils.visibleOrGone
import com.google.android.gms.ads.AdRequest
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.compress_dialog_native_ad.view.*
import kotlinx.android.synthetic.main.item_compressing_image.view.*

class CompressingImageAdapter(
        private val context: Context,
        private val list: Array<SelectedImage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    //private val adRequest: AdRequest
    private val AD_THRESHOLD = 6
    private val TYPE_AD = 1
    private val TYPE_IMAGE = 0
    val imageCompressedColor = R.color.green_800.getColor(context)
    val imageEnhancedColor = R.color.red_800.getColor(context)
    private val adRequest: AdRequest by lazy {
        AdRequest.Builder().build()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        if (viewType == TYPE_AD) {
            return AdViewHolder(layoutInflater.inflate(R.layout.compress_dialog_native_ad, parent, false))
        }
        return ImageViewHolder(layoutInflater.inflate(R.layout.item_compressing_image, parent, false))
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ImageViewHolder) {
            val positionInList = getDataSetPosition(position)
            if (positionInList < list.size) {
                holder.bind(list[positionInList])
            }
        } else if (holder is AdViewHolder) {
        }
    }

    override fun getItemCount(): Int {
        return list.size + list.size / AD_THRESHOLD
    }

    override fun getItemViewType(position: Int): Int {
        return if (position != 0 && (position + 1) % (AD_THRESHOLD + 1) == 0) TYPE_AD else TYPE_IMAGE
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        if (holder is ImageViewHolder) {
            holder.itemView.imv_bitmap.setImageDrawable(null)
        }
    }

    fun updateItem(dataSetPosition: Int) {
        notifyItemChanged(getAdapterPosition(dataSetPosition))
    }

    private fun getAdapterPosition(dataSetPosition: Int): Int {
        return dataSetPosition + dataSetPosition / AD_THRESHOLD
    }

    private fun getDataSetPosition(adapterPosition: Int): Int {
        return adapterPosition - (adapterPosition / (AD_THRESHOLD + 1))
    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Palette.PaletteAsyncListener {

        lateinit var selectedImage: SelectedImage

        //selected image target
        private val imageTarget = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    itemView.imv_bitmap.setImageBitmap(it)
                    if (selectedImage.color == null) {
                        Palette.from(it).generate(this@ImageViewHolder)
                    } else {
                        setImageSizeTint()
                    }
                }
            }
        }


        fun bind(image: SelectedImage) {
            selectedImage = image
            itemView.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imv_bitmap.clipToOutline = true
                }

                //setting image, title, and progress
                Picasso.get()
                        .load(ImageFileProvider.getImageUri(context, image.path))
                        .into(imageTarget)

                //setting original file size and name
                val fileSize = Utils.convertSize(image.length().toFloat(), 0)
                tv_bitmap_size.text = context.getString(R.string.file_size, fileSize.first, fileSize.second)
                tv_file_name.text = image.name

                //compressed by label and progress bar
                tv_compressed_by.visibleOrGone(image.isCompressed)
                pb_compressing_image.isIndeterminate = !image.isCompressed

                if (image.isCompressed) {
                    pb_compressing_image.progress = pb_compressing_image.max

                    //setting compressed by & its background
                    val compressedBy = Utils.findPercentDiff(image.compressImageBytes, image.length()).toInt()
                    if (compressedBy <= 0) {
                        tv_compressed_by.text = context.getString(R.string.percentage_compressed, Math.abs(compressedBy))
                        tv_compressed_by.setBackgroundTint(imageCompressedColor)
                    } else {
                        tv_compressed_by.text = context.getString(R.string.percentage_decompressed, compressedBy)
                        tv_compressed_by.setBackgroundTint(imageEnhancedColor)
                    }

                    //compressed file size
                    val compressFileSize = Utils.convertSize(image.compressImageBytes.toFloat())
                    tv_compressed_size.text = context.getString(R.string.file_size, compressFileSize.first, compressFileSize.second)
                }
                anim_done.visibleOrGone(image.isCompressed)
            }
        }

        //on palette color generated
        override fun onGenerated(palette: Palette?) {
            palette?.let { palette ->
                val color = palette.getMutedColor(Color.parseColor("#EEEEEE"))
                val textColor = palette.mutedSwatch?.titleTextColor ?: Color.parseColor("#424242")
                if (selectedImage.color == null) {
                    selectedImage.color = color
                    selectedImage.textColor = textColor
                }
                setImageSizeTint()
            }
        }

        private fun setImageSizeTint() {
            ViewCompat.setBackgroundTintList(itemView.scrim_image, ColorStateList.valueOf(selectedImage.color!!))
            itemView.tv_bitmap_size.setTextColor(selectedImage.textColor!!)
        }
    }

    inner class AdViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.nativeAdView.loadAd(adRequest)
        }
    }
}