package com.picker.file.factory

import com.acorns.android.refactored.components.filePicker.source.GalleryPicker
import com.picker.file.source.CameraPicker
import com.picker.file.source.FilePicker

object FileSourceFactory {

    @JvmStatic
    fun createImageSource(sourceType: FileSourceType): FilePicker {
        when (sourceType) {
            FileSourceType.CAMERA -> return CameraPicker()
            FileSourceType.GALLERY -> return GalleryPicker()
            else -> throw RuntimeException()
        }
    }
}