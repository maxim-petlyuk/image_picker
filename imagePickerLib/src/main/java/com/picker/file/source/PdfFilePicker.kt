//package com.acorns.android.refactored.components.filePicker.source
//
//import android.app.Activity
//import android.content.Context
//import android.content.Intent
//import android.support.v4.app.Fragment
//import com.picker.file.FilePickerConstants
//import com.acorns.android.refactored.components.filePicker.result.RegistrationFile
//import com.file.picker.R
//import com.file.picker.source.BaseFilePicker
//
//class PdfFilePicker : BaseFilePicker() {
//
//    private val FILE_TYPE = "application/pdf"
//
//    override fun loadFile(context: Context) {
//        (context as Activity).startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_PDF)
//    }
//
//    override fun loadFile(fragment: Fragment) {
//        fragment.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_PDF)
//    }
//
//    private fun createIntent(): Intent {
//        return Intent.createChooser(
//                Intent().setType(FILE_TYPE).setAction(Intent.ACTION_GET_CONTENT), "PDF")
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (resultCode != Activity.RESULT_OK) {
//            return
//        }
//
//        when (requestCode) {
//            FilePickerConstants.REQUEST_CODE_PDF -> {
//                mFileLoaderCallbackCallbacks.onEach { callback ->
//                    data?.data?.let { fileUri ->
//                        callback.onFileReady(RegistrationFile(fileUri, R.drawable.ic_pdf_logo))
//                    } ?: run {
//                        callback.onError(IllegalStateException("Pdf file path is empty"))
//                    }
//                }
//            }
//        }
//    }
//
//    override fun onRequestPermissionsResult(fragment: Fragment, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {}
//}