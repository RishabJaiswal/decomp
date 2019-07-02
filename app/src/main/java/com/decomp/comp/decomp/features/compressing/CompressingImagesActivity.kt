package com.decomp.comp.decomp.features.compressing

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.darsh.multipleimageselect.models.Image
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_COMP_FACTOR
import com.decomp.comp.decomp.application.KEY_IMAGES
import kotlinx.android.synthetic.main.activity_compressing_images.*

class CompressingImagesActivity : AppCompatActivity() {

    private val images: Array<SelectedImage> by lazy {
        intent.getParcelableArrayListExtra<Image>(KEY_IMAGES).let { images ->
            Array(images.size) {
                SelectedImage(images[it])
            }
        }
    }

    private val compFactor by lazy {
        intent.getIntExtra(KEY_COMP_FACTOR, 100)
    }

    private val compressingAdapter by lazy {
        CompressingImageAdapter(this, images)
    }

    private val baseDir: String by lazy {
        getSharedPreferences("dir", Context.MODE_PRIVATE).getString("dir", null)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compressing_images)
        rv_images.adapter = compressingAdapter
        Handler().postDelayed({
            CompressTask(compFactor, baseDir, this::updateItem, this::onCompressionComplete)
                    .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *images)
        }, 2000)
    }

    private fun updateItem(position: Int) {
        compressingAdapter.notifyItemChanged(position)
    }

    private fun onCompressionComplete(string: String?) {
    }
}
