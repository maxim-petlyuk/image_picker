package com.picker.file.converters

import android.net.Uri
import android.util.Base64
import io.reactivex.Single

class Base64Converter : ImageConverter<String> {

    private val bytesConverter: BytesConverter = BytesConverter()

    override fun transform(fileUri: Uri): Single<out String> {
        return bytesConverter.transform(fileUri)
                .flatMap { Single.just(Base64.encodeToString(it, Base64.DEFAULT)) }
    }
}