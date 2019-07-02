package com.decomp.comp.decomp.features.compressing

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import java.io.*
import java.util.*

class CompressTask(private val compFactor: Int,
                   private val baseDir: String,
                   private val updateItem: (Int) -> Unit,
                   private val onComplete: (String?) -> Unit) : AsyncTask<SelectedImage, Int, String>() {


    private val compressedBytesOutputStream = ByteArrayOutputStream()
    private var compressedImgBytes = ByteArray(0)
    private var totalCompressedBytes = 0L
    private var filePath = ""

    override fun doInBackground(vararg imgs: SelectedImage): String {
        imgs.forEachIndexed { index, image ->
            try {
                compressedBytesOutputStream.reset()
                filePath = image.absolutePath ?: ""
                if (filePath.isNotEmpty()) {
                    BitmapFactory.decodeFile(filePath)?.let { bitmap ->
                        bitmap.compress(Bitmap.CompressFormat.JPEG, compFactor, compressedBytesOutputStream)
                    }
                }

                //compressing
                if (filePath.isNotEmpty() && compressedBytesOutputStream.size() != 0) {
                    compressedImgBytes = compressedBytesOutputStream.toByteArray()

                    //writing compressed bytes to a file
                    BufferedOutputStream(
                            FileOutputStream(
                                    File(baseDir + File.separator + Calendar.getInstance().timeInMillis.toString()
                                            + "."
                                            + filePath.substring(filePath.lastIndexOf(".") + 1))))
                            .write(compressedImgBytes)

                    //publishing changes
                    image.isCompressed = true
                    image.compressImageBytes = compressedImgBytes.size.toLong()
                    totalCompressedBytes += compressedImgBytes.size.toLong()
                    publishProgress(index)
                }
            } catch (e: IOException) {
                cancel(true)
            }
        }
        return imgs.size.toString()
    }

    override fun onProgressUpdate(vararg values: Int?) {
        values.forEach {
            it?.let { position ->
                updateItem(position)
            }
        }
    }

    override fun onPostExecute(result: String?) {
        onComplete(result)
    }
}