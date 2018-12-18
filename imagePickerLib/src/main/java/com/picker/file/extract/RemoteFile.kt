package com.picker.file.extract

import android.content.Context
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import io.reactivex.Single
import java.io.File
import java.io.FileOutputStream

class RemoteFile(private val path: Uri, private val fileName: String) : PickedFile {

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(this.path, flags)
        dest.writeString(this.fileName)
    }

    protected constructor(parcel: Parcel) :
            this(parcel.readParcelable(Uri::class.java.classLoader), parcel.readString())

    override fun getFile(context: Context): Single<File> {
        return Single.create { singleSubscriber ->
            val tmpDir = context.cacheDir.toString() + "/tempFiles"
            File(tmpDir).mkdir()

            val fileName = fileName.substring(fileName.lastIndexOf('/') + 1)
            val file = File(File(tmpDir), fileName)

            try {
                FileOutputStream(file).use { outputStream ->
                    context.contentResolver.openInputStream(path).use { inputStream ->
                        val buf = ByteArray(8192)
                        while (!singleSubscriber.isDisposed) {
                            val count = inputStream.read(buf, 0, buf.size)

                            if (count < 0) {
                                break
                            }

                            outputStream.write(buf, 0, count)
                            outputStream.flush()
                        }

                        if (!singleSubscriber.isDisposed) {
                            singleSubscriber.onSuccess(file)
                        }
                    }
                }
            } catch (e: Exception) {
                if (!singleSubscriber.isDisposed) {
                    singleSubscriber.onError(e)
                }
            }
        }
    }

    companion object CREATOR : Parcelable.Creator<RemoteFile> {
        override fun createFromParcel(parcel: Parcel): RemoteFile {
            return RemoteFile(parcel)
        }

        override fun newArray(size: Int): Array<RemoteFile?> {
            return arrayOfNulls(size)
        }
    }
}
