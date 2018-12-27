package com.picker.file

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import com.picker.file.extract.RealPathExtractor
import com.picker.file.factory.FileSourceFactory
import com.picker.file.factory.FileSourceType
import com.picker.file.source.FilePicker
import com.picker.file.source.LifeCycle
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.subjects.AsyncSubject
import io.reactivex.subjects.Subject

open class RxFilePicker : LifeCycle {

    private var pickerResultSubject: Subject<PickerResult>? = null
    private val filePathExtractor = RealPathExtractor()
    private var picker: FilePicker? = null

    companion object {

        @Volatile
        private var instance: RxFilePicker? = null

        @JvmStatic
        fun getInstance(): RxFilePicker =
                instance ?: synchronized(this) {
                    instance ?: buildInstance().also { instance = it }
                }

        private fun buildInstance() = RxFilePicker()
    }

    fun fromSource(sourceType: FileSourceType): RxFilePicker {
        picker = FileSourceFactory.createFilePicker(sourceType)
        return this
    }

    fun pickFile(activity: Activity): Single<PickerResult> {
        pickerResultSubject = AsyncSubject.create<PickerResult>()

        picker?.let { safePicker ->
            safePicker.setResultCallback(pickerResultSubject)
            safePicker.pickFile(activity)
        } ?: throw RuntimeException("Picker is not initialized")

        return decorateResult(activity, pickerResultSubject!!)
    }

    fun pickFile(fragment: Fragment): Single<PickerResult> {
        pickerResultSubject = AsyncSubject.create<PickerResult>()

        picker?.let { safePicker ->
            safePicker.setResultCallback(pickerResultSubject)
            safePicker.pickFile(fragment)
        } ?: throw RuntimeException("Picker is not initialized")

        return decorateResult(fragment.context, pickerResultSubject!!)
    }

    private fun decorateResult(context: Context?, pickerSubscription: Subject<PickerResult>): Single<PickerResult> {
        return pickerSubscription
                .singleOrError()
                .flatMap { result ->
                    context?.let { context ->
                        return@flatMap filePathExtractor.getRealPath(context, result.filePath)
                                .getFile(context)
                                .map { pickedFile -> PickerResult(Uri.fromFile(pickedFile)) }
                    } ?: run {
                        return@flatMap Single.error<PickerResult>(IllegalStateException("Context is null"))
                    }
                }
    }

    fun hasActiveSubscription() = pickerResultSubject != null

    fun getActiveSubscription(context: Context?): Single<PickerResult> {
        val activeSubctiption = pickerResultSubject!!
        pickerResultSubject = null
        return decorateResult(context, activeSubctiption)
    }

    fun onSaveInstanceState(outState: Bundle) {
        picker?.let { filePicker -> outState.putParcelable(FilePickerConstants.ARG_PICKER, picker) }
    }

    fun onRestoreInstanceState(savedInstanceState: Bundle) {
        picker = savedInstanceState.getParcelable(FilePickerConstants.ARG_PICKER) as FilePicker?
    }

    override fun onRequestPermissionsResult(pickerContext: Any, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        picker?.onRequestPermissionsResult(pickerContext, requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        picker?.onActivityResult(requestCode, resultCode, data)
    }
}

