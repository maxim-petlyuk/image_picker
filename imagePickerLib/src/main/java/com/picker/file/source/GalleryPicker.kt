package com.acorns.android.refactored.components.filePicker.source

import android.app.Activity
import android.content.Intent
import android.support.v4.app.Fragment
import com.picker.file.FilePickerConstants
import com.picker.file.PickerResult
import com.picker.file.source.BaseFilePicker
import com.picker.file.source.LifeCycle
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe

class GalleryPicker : BaseFilePicker() {

    val IMAGE_TYPE = "image/*"

    override fun pickFile(activity: Activity): Single<PickerResult> {
        val onSubscribe = RxGalleryPickerOnSubscribe(activity)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
    }

    override fun pickFile(fragment: Fragment): Single<PickerResult> {
        val onSubscribe = RxGalleryPickerOnSubscribe(fragment)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
    }

    private inner class RxGalleryPickerOnSubscribe(private val pickerContext: Any) : SingleOnSubscribe<PickerResult>, LifeCycle {

        private var emitter: SingleEmitter<PickerResult>? = null

        override fun subscribe(emitter: SingleEmitter<PickerResult>) {
            this.emitter = emitter
            try {
                doPick(pickerContext, emitter)
            } catch (error: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(error)
                }
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }

            when (requestCode) {
                FilePickerConstants.REQUEST_CODE_GALLERY -> {
                    emitter?.let { singleEmitter ->
                        data?.data?.let { filePath ->
                            singleEmitter.onSuccess(PickerResult(filePath))
                        } ?: run {
                            singleEmitter.onError(RuntimeException("There is no file result"))
                        }
                    }
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        }

        private fun doPick(pickerContext: Any, emitter: SingleEmitter<PickerResult>) {
            if (pickerContext is Activity) {
                pickerContext.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
            } else if (pickerContext is Fragment && pickerContext.context != null) {
                pickerContext.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
            } else {
                emitter.onError(java.lang.IllegalStateException())
            }
        }

        private fun createIntent(): Intent {
            return Intent.createChooser(Intent().setType(IMAGE_TYPE).setAction(Intent.ACTION_GET_CONTENT), "Image")
        }
    }
}