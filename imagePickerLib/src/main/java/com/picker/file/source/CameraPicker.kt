package com.picker.file.source

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import com.picker.file.BuildConfig
import com.picker.file.FilePickerConstants
import com.picker.file.PickerResult
import com.picker.file.exceptions.PermissionNotGrantedException
import com.picker.file.exceptions.RepeatRequiresPermissionException
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.functions.BiPredicate
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraPicker : BaseFilePicker() {

    override fun pickFile(activity: Activity): Single<PickerResult> {
        val onSubscribe = RxCameraPickerOnSubscribe(activity)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
                .retry(BiPredicate { count, error ->
                    return@BiPredicate error is RepeatRequiresPermissionException
                })
    }

    override fun pickFile(fragment: Fragment): Single<PickerResult> {
        val onSubscribe = RxCameraPickerOnSubscribe(fragment)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
                .retry(BiPredicate { count, error ->
                    return@BiPredicate error is RepeatRequiresPermissionException
                })
    }

    private inner class RxCameraPickerOnSubscribe(private val pickerContext: Any) : SingleOnSubscribe<PickerResult>, LifeCycle {

        private var savedFilePath: Uri? = null
        private var emitter: SingleEmitter<PickerResult>? = null

        override fun subscribe(emitter: SingleEmitter<PickerResult>) {
            try {
                this.emitter = emitter
                val permissionGranted = checkIfPermissionGranted(pickerContext, Manifest.permission.CAMERA)
                if (permissionGranted) {
                    doPick(pickerContext, emitter)
                } else {
                    requestPermission(pickerContext, FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS, Manifest.permission.CAMERA)
                }
            } catch (error: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(error)
                }
            }
        }

        private fun doPick(pickerContext: Any, emitter: SingleEmitter<PickerResult>) {
            if (pickerContext is Activity) {
                pickerContext.startActivityForResult(createIntent(pickerContext), FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA)
            } else if (pickerContext is Fragment && pickerContext.context != null) {
                pickerContext.startActivityForResult(createIntent(pickerContext.context!!), FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA)
            } else {
                emitter.onError(java.lang.IllegalStateException())
            }
        }

        private fun createIntent(context: Context): Intent {
            val photoFile = createImageFile(context)

            if (!photoFile.exists()) {
                photoFile.mkdir()
            }

            savedFilePath = Uri.fromFile(photoFile)
            val photoURI = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, photoURI)
            cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            return cameraIntent
        }

        @Throws(IOException::class)
        private fun createImageFile(context: Context): File {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_" + timeStamp + "_"
            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            val image = File.createTempFile(
                    imageFileName, /* prefix */
                    ".jpg", /* suffix */
                    storageDir     /* directory */
            )
            return image
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }

            when (requestCode) {
                FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA -> {
                    emitter?.let { singleEmitter ->
                        if (!singleEmitter.isDisposed) {
                            savedFilePath?.let { filePath ->
                                singleEmitter.onSuccess(PickerResult(filePath))
                            } ?: run {
                                singleEmitter.onError(RuntimeException("There is no file result"))
                            }
                        } else {
                            lifeCycleSet.remove(this)
                        }
                    }
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            when (requestCode) {
                FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS -> {
                    emitter?.let { singleEmitter ->
                        if (!singleEmitter.isDisposed) {
                            val cameraPermissionGranted = isRequestedPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA)
                            if (cameraPermissionGranted) {
                                singleEmitter.onError(RepeatRequiresPermissionException())
                            } else {
                                singleEmitter.onError(PermissionNotGrantedException())
                            }
                        } else {
                            lifeCycleSet.remove(this)
                        }
                    }
                }
            }
        }
    }
}