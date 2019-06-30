package com.decomp.comp.decomp.features.compressing

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.darsh.multipleimageselect.models.Image
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_COMP_FACTOR
import com.decomp.comp.decomp.application.KEY_IMAGES
import kotlinx.android.synthetic.main.activity_compressing_images.*

class CompressingImagesActivity : AppCompatActivity() {

    private val images: List<Image> by lazy {
        intent.getParcelableArrayListExtra<Image>(KEY_IMAGES)
    }

    private val compFactor by lazy {
        intent.getIntExtra(KEY_COMP_FACTOR, 100)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compressing_images)
        rv_images.adapter = CompressingImageAdapter(this, images.map { SelectedImage(it) })
    }
}
