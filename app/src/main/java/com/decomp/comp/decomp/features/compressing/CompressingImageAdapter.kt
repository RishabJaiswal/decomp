package com.decomp.comp.decomp.features.compressing

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.CompressTaskFragment
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.ImageFileProvider
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import kotlinx.android.synthetic.main.item_compressing_image.view.*

class CompressingImageAdapter(
        private val context: Context,
        private val list: List<SelectedImage>
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

        //selected image target
        private val imageTarget = object : Target {
            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            }

            override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            }

            override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                bitmap?.let {
                    itemView.imv_bitmap.setImageBitmap(it)
                    Palette.from(it).generate(this@ViewHolder)
                }
            }
        }


        fun bind(image: SelectedImage) {
            itemView.apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    imv_bitmap.clipToOutline = true
                }

                //setting image, title, and progress
                Picasso.get()
                        .load(ImageFileProvider.getImageUri(context, image.path))
                        .into(imageTarget)
                tv_file_name.text = image.name
                if (image.isCompressed) {
                    pb_compressing_image.progress = pb_compressing_image.max
                }
                tv_bitmap_size.text = CompressTaskFragment.converter(image.length().toFloat(), 0)
            }
        }

        //on palette color generated
        override fun onGenerated(palette: Palette?) {
            palette?.let {
                val color = it.getVibrantColor(Color.RED)
                ColorStateList.valueOf(color)?.let { colorStateList ->
                    ViewCompat.setBackgroundTintList(itemView.scrim_image, colorStateList)
                    setProgressColor(color)
                }
            }
        }

        private fun setProgressColor(color: Int) {
            itemView.pb_compressing_image.apply {
                val progressDrawable = indeterminateDrawable.mutate()
                progressDrawable.setColorFilter(color, PorterDuff.Mode.SRC_IN)
                this.progressDrawable = progressDrawable
            }
        }
    }
}