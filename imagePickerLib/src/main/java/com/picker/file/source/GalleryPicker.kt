//package com.acorns.android.refactored.components.filePicker.source
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.support.v4.app.Fragment
//import com.picker.file.FilePickerConstants
//import com.acorns.android.refactored.components.filePicker.result.RegistrationFile
//import com.file.picker.source.BaseFilePicker
//import java.lang.IllegalStateException
//
//class GalleryPicker : BaseFilePicker() {
//
//    val IMAGE_TYPE = "image/*"
//
//    override fun loadFile(context: Context) {
//        (context as Activity).startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
//    }
//
//    override fun loadFile(fragment: Fragment) {
//        fragment.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
//    }
//
//    private fun createIntent(): Intent {
//        return Intent.createChooser(
//                Intent().setType(IMAGE_TYPE).setAction(Intent.ACTION_GET_CONTENT), "Image")
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode != Activity.RESULT_OK) {
//            return
//        }
//
//        when (requestCode) {
//            FilePickerConstants.REQUEST_CODE_GALLERY -> {
//                mFileLoaderCallbackCallbacks.onEach { callback ->
//                    data?.data?.let { filePath ->
//                        callback.onFileReady(RegistrationFile(filePath))
//                    } ?: run {
//                        callback.onError(IllegalStateException());
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(fragment: Fragment, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
//    }
//}