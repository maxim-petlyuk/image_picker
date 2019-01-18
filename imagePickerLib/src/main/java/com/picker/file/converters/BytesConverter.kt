package com.picker.file.converters

import android.net.Uri
import io.reactivex.Single
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException

class BytesConverter : ImageConverter<ByteArray>{

    override fun transform(fileUri: Uri): Single<out ByteArray> {
        return Single.create { emitter ->
            try {
                val imageFile = File(fileUri.path)

                if (!imageFile.exists()) {
                    throw FileNotFoundException()
                }

                val inputStream = FileInputStream(imageFile.path)
                val buffer = ByteArray(8192)
                val bytes: ByteArray?
                var bytesRead: Int
                val output = ByteArrayOutputStream()

                while (!emitter.isDisposed) {
                    bytesRead = inputStream.read(buffer)
                    if (bytesRead == -1) {
                        break
                    }
                    output.write(buffer, 0, bytesRead)
                }

                bytes = output.toByteArray()

                if (!emitter.isDisposed) {
                    if (bytes == null) {
                        emitter.onError(IllegalStateException())
                    } else {
                        emitter.onSuccess(bytes)
                    }
                }
            } catch (throwable: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(throwable)
                }
            }
        }
    }
}