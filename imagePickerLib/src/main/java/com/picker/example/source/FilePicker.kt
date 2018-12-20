package com.acorns.android.refactored.components.filePicker.source

import android.app.Activity
import android.support.v4.app.Fragment
import com.picker.picker.PickerResult
import com.picker.picker.source.LifeCycle
import io.reactivex.Single

interface FilePicker : LifeCycle {

    fun pickFile(activity: Activity): Single<PickerResult>

    fun pickFile(fragment: Fragment): Single<PickerResult>
}