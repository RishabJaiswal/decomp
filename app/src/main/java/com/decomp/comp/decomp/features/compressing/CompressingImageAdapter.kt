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
import com.decomp.comp.decomp.utils.visibleOrGone
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_compressing_image.view.*

class CompressingImageAdapter(
        private val context: Context,
        private val list: Array<SelectedImage>
) : RecyclerView.Adapter<CompressingImageAdapter.ViewHolder>() {


    //private val adRequest: AdRequest
    private val AD_THRESHOLD = 5
    private val TYPE_AD = 1
    private val TYPE_IMAGE = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_compressing_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), Palette.PaletteAsyncListener {

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
                        Palette.from(it).generate(this@ViewHolder)
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

                //setting original file size
                val fileSize = Utils.convertSize(image.length().toFloat(), 0)
                tv_bitmap_size.text = context.getString(R.string.file_size, fileSize.first, fileSize.second)
                tv_file_name.text = image.name

                pb_compressing_image.isIndeterminate = !image.isCompressed
                if (image.isCompressed) {
                    pb_compressing_image.progress = pb_compressing_image.max
                }
                tv_compressed_by.visibleOrGone(image.isCompressed)
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
}