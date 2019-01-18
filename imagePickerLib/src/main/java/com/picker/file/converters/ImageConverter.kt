package com.picker.file.converters

import android.net.Uri
import io.reactivex.Single

interface ImageConverter<out T> {

    fun transform(fileUri: Uri): Single<out T>
}