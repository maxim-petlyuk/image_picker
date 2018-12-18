package com.picker.file.source

import android.app.Activity
import android.support.v4.app.Fragment
import com.picker.file.PickerResult
import com.picker.file.source.LifeCycle
import io.reactivex.Single

interface FilePicker : LifeCycle {

    fun pickFile(activity: Activity): Single<PickerResult>

    fun pickFile(fragment: Fragment): Single<PickerResult>
}