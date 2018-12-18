package com.picker.file.extract

import android.content.Context
import android.os.Parcelable

import java.io.File

import io.reactivex.Single

interface PickedFile : Parcelable {

    fun getFile(context: Context): Single<File>
}
