package com.picker.file.factory

import com.picker.file.source.CameraPicker
import com.picker.file.source.FilePicker

internal object FileSourceFactory {

    @JvmStatic
    internal fun createFilePicker(sourceType: FileSourceType): FilePicker {
        when (sourceType) {
            FileSourceType.CAMERA -> return CameraPicker()
//            FileSourceType.GALLERY -> return GalleryPicker()
            else -> throw RuntimeException()
        }
    }
}