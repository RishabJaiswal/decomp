package com.decomp.comp.decomp.features.compressing

import com.nguyenhoanglam.imagepicker.model.Image
import java.io.File

class SelectedImage(val image: Image) : File(image.path) {
    var isCompressed: Boolean = false
    var color: Int? = null
    var textColor: Int? = null
    var compressImageBytes: Long = 0L
}