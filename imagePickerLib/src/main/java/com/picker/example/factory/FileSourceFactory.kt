package com.picker.picker.factory

import com.acorns.android.refactored.components.filePicker.source.CameraPicker
//import com.acorns.android.refactored.components.filePicker.source.GalleryPicker
import com.acorns.android.refactored.components.filePicker.source.FilePicker
//import com.acorns.android.refactored.components.filePicker.source.PdfFilePicker

object FileSourceFactory {

    fun createImageSource(sourceType: FileSourceType): FilePicker {
        when (sourceType) {
//            FileSourceType.GALLERY -> return GalleryPicker()
            FileSourceType.CAMERA -> return CameraPicker()
//            FileSourceType.PDF -> return PdfFilePicker()
            else -> throw RuntimeException()
        }
    }
}