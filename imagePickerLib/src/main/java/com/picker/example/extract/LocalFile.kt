package com.picker.picker.extract

import android.content.Context
import android.os.Parcel
import android.os.Parcelable

import java.io.File

import io.reactivex.Single

class LocalFile(private var filePath: String) : PickedFile {

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(this.filePath)
    }

    protected constructor(parcel: Parcel) : this(parcel.readString())

    override fun getFile(context: Context): Single<File> {
        return Single.just(File(filePath))
    }

    companion object CREATOR : Parcelable.Creator<LocalFile> {
        override fun createFromParcel(parcel: Parcel): LocalFile {
            return LocalFile(parcel)
        }

        override fun newArray(size: Int): Array<LocalFile?> {
            return arrayOfNulls(size)
        }
    }
}
