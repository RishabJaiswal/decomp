package com.decomp.comp.decomp.features.compressing

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.darsh.multipleimageselect.models.Image
import com.decomp.comp.decomp.CompGallery
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.application.KEY_COMP_FACTOR
import com.decomp.comp.decomp.application.KEY_IMAGES
import com.decomp.comp.decomp.utils.Utils
import com.decomp.comp.decomp.utils.extensions.toggle
import com.decomp.comp.decomp.utils.visible
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_compressing_images.*
import kotlinx.android.synthetic.main.bottom_sheet_compressing_images.*

class CompressingImagesActivity : AppCompatActivity(), View.OnClickListener {

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

    private val compressProgressBottomSheet by lazy {
        BottomSheetBehavior.from(bs_compress_progress)
    }

    private var isCompressionComplete = false

    private lateinit var compressTask: CompressTask

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compressing_images)
        rv_images.adapter = compressingAdapter
        Handler().postDelayed({
            compressTask = CompressTask(compFactor, baseDir, this::updateItem, this::onCompressionComplete)
            compressTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, *images)
        }, 2000)
        setTotalUncompressedSize()
        pb_compressing.max = images.size
        compressProgressBottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        btn_done_compressing.setOnClickListener(this)
        bs_compress_progress.setOnClickListener(this)
    }

    override fun onBackPressed() {
        if (!isCompressionComplete) {
            AlertDialog.Builder(this)
                    .setTitle(R.string.title_stop_compressnig)
                    .setMessage(R.string.msg_stop_compressnig)
                    .setPositiveButton(R.string.cta_no) { _, _ ->
                    }
                    .setNegativeButton(R.string.cta_yes) { _, _ ->
                        //cancelling task
                        if (compressTask.status == AsyncTask.Status.RUNNING ||
                                compressTask.status == AsyncTask.Status.PENDING) {
                            compressTask.cancel(true)
                        }
                        finish()
                    }
                    .create()
                    .show()
        } else {
            startActivity(Intent(this, CompGallery::class.java))
            finish()
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.btn_done_compressing -> {
                startActivity(Intent(this, CompGallery::class.java))
                finish()
            }
            R.id.bs_compress_progress -> {
                compressProgressBottomSheet.toggle()
            }
        }
    }

    private fun updateItem(position: Int, imagesCompressed: Int, totalCompressedBytes: Long) {
        compressingAdapter.notifyItemChanged(position)
        pb_compressing.progress = imagesCompressed
        tv_compress_progress.text = getString(R.string.compress_progress, imagesCompressed, images.size)
        Utils.convertSize(totalCompressedBytes.toFloat()).apply {
            tv_compressed_bytes.text = getString(R.string.file_size, this.first, this.second)
        }
    }

    private fun onCompressionComplete(string: String?) {
        tv_lbl_compressing.text = getString(R.string.compressed)
        compressProgressBottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
        btn_done_compressing.visible()
        isCompressionComplete = true
    }

    private fun setTotalUncompressedSize() {
        var totalSize = 0L
        images.forEach { image ->
            totalSize += image.length()
        }

        //setting formatted uncompressed files size
        Utils.convertSize(totalSize.toFloat()).apply {
            tv_uncompressed_size.text = getString(R.string.file_size, this.first, this.second)
        }
    }
}
