package com.picker.file.source

import android.app.Activity
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.picker.file.PickerResult
import io.reactivex.subjects.Subject

interface FilePicker : LifeCycle, Parcelable {

    var pickerResultSubject: Subject<PickerResult>?

    fun pickFile(activity: Activity)

    fun pickFile(fragment: Fragment)
}