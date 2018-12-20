package com.acorns.android.refactored.components.filePicker.source

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import com.acorns.android.refactored.components.filePicker.FilePickerConstants
import com.picker.picker.BuildConfig
import com.picker.picker.PickerResult
import com.picker.picker.exceptions.PermissionNotGrantedException
import com.picker.picker.exceptions.RepeatRequiresPermission
import com.picker.picker.source.BaseFilePicker
import com.picker.picker.source.LifeCycle
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraPicker : BaseFilePicker() {

    override fun pickFile(activity: Activity): Single<PickerResult> {
        val onSubscribe = RxPickerOnSubscribe(activity)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
    }

    override fun pickFile(fragment: Fragment): Single<PickerResult> {
        val onSubscribe = RxPickerOnSubscribe(fragment)
        lifeCycleSet.add(onSubscribe)
        return Single.create<PickerResult>(onSubscribe)
    }

    private inner class RxPickerOnSubscribe(private val pickerContext: Any) : SingleOnSubscribe<PickerResult>, LifeCycle {

        private var savedFilePath: String = ""
        private var emitter: SingleEmitter<PickerResult>? = null

        override fun subscribe(emitter: SingleEmitter<PickerResult>) {
            try {
                this.emitter = emitter
                val permissionGranted = checkOnPermission(pickerContext)
                if (permissionGranted) {
                    doPick(pickerContext, emitter)
                } else {
                    requestPermission(pickerContext)
                }
            } catch (error: Throwable) {
                if (!emitter.isDisposed) {
                    emitter.onError(error)
                }
            }
        }

        private fun doPick(pickerContext: Any, emitter: SingleEmitter<PickerResult>) {
            if (pickerContext is Activity) {
                pickerContext.startActivityForResult(createIntent(pickerContext), FilePickerConstants.REQUEST_CODE_GALLERY)
            } else if (pickerContext is Fragment && pickerContext.context != null) {
                pickerContext.startActivityForResult(createIntent(pickerContext.context!!), FilePickerConstants.REQUEST_CODE_GALLERY)
            } else {
                emitter.onError(java.lang.IllegalStateException())
            }
        }

        private fun createIntent(context: Context): Intent {
            val photoFile = createImageFile(context)
            savedFilePath = Uri.fromFile(photoFile).toString()
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
                    storageDir      /* directory */
            )
            return image
        }

        private fun requestPermission(pickerContext: Any) {
            if (pickerContext is Activity) {
                ActivityCompat.requestPermissions(pickerContext, arrayOf(Manifest.permission.CAMERA), FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS)
            }

            if (pickerContext is Fragment) {
                pickerContext.requestPermissions(arrayOf(Manifest.permission.CAMERA), FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS)
            }
        }

        private fun checkOnPermission(pickerContext: Any): Boolean {
            var determineContext: Context? = null

            if (pickerContext is Activity) {
                determineContext = pickerContext
            }

            if (pickerContext is Fragment) {
                determineContext = pickerContext.context
            }

            return determineContext?.let { safeContext ->
                return ActivityCompat.checkSelfPermission(safeContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            } ?: run {
                throw IllegalStateException("Context is not determined")
            }
        }

        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode != Activity.RESULT_OK) {
                return
            }

            when (requestCode) {
                FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA -> {
                    emitter?.let { singleEmitter ->
                        if (!singleEmitter.isDisposed) {
                            singleEmitter.onSuccess(PickerResult(savedFilePath))
                        }
                    }
                }
            }
        }

        override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
            when (requestCode) {
                FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS -> {
                    val cameraPermissionGranted = isPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA)
                    if (cameraPermissionGranted) {
                        throw RepeatRequiresPermission()
                    } else {
                        throw PermissionNotGrantedException()
                    }
                }
            }
        }
    }
}