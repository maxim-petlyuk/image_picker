package com.picker.file.source

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import com.picker.file.BuildConfig
import com.picker.file.FilePickerConstants
import com.picker.file.PickerResult
import com.picker.file.exceptions.PermissionNotGrantedException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class CameraPicker() : BaseFilePicker() {

    private var savedFilePath: Uri? = null

    override fun pickFile(activity: Activity) {
        doPickOperation(activity)
    }

    override fun pickFile(fragment: Fragment) {
        doPickOperation(fragment)
    }

    private fun doPickOperation(pickerContext: Any) {
        val permissionGranted = checkIfPermissionGranted(pickerContext, Manifest.permission.CAMERA)

        if (permissionGranted) {
            requestPick(pickerContext)
        } else {
            requestPermission(pickerContext, FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS, Manifest.permission.CAMERA)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA -> {
                savedFilePath?.let { filePath ->
                    pickerSubject?.onNext(PickerResult(filePath))
                    pickerSubject?.onComplete()
                } ?: run {
                    pickerSubject?.onError(RuntimeException("There is no file result"))
                }
            }
        }
    }

    override fun onRequestPermissionsResult(pickerContext: Any, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FilePickerConstants.REQUEST_CODE_CAMERA_PERMISSIONS -> {
                val cameraPermissionGranted = isRequestedPermissionGranted(permissions, grantResults, Manifest.permission.CAMERA)
                if (cameraPermissionGranted) {
                    requestPick(pickerContext)
                } else {
                    pickerSubject?.onError(PermissionNotGrantedException())
                }
            }
        }
    }

    private fun requestPick(pickerContext: Any) {
        if (pickerContext is Activity) {
            pickerContext.startActivityForResult(createIntent(pickerContext), FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA)
        } else if (pickerContext is Fragment && pickerContext.context != null) {
            pickerContext.startActivityForResult(createIntent(pickerContext.context!!), FilePickerConstants.REQUEST_CODE_DEVICE_CAMERA)
        } else {
            pickerSubject?.onError(IllegalArgumentException("pickerContext could be only Activity or Fragment"))
        }
    }

    private fun createIntent(context: Context): Intent {
        val photoFile = createImageFile(context)

        if (!photoFile.exists()) {
            photoFile.mkdir()
        }

        savedFilePath = Uri.fromFile(photoFile)
        val photoURI = FileProvider.getUriForFile(context, context.packageName + "." + BuildConfig.APPLICATION_ID + ".fileprovider", photoFile)
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
                storageDir    /* directory */
        )
        return image
    }

    constructor(parcel: Parcel) : this() {
        savedFilePath = parcel.readParcelable(Uri::class.java.classLoader)
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(savedFilePath, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CameraPicker> {
        override fun createFromParcel(parcel: Parcel): CameraPicker {
            return CameraPicker(parcel)
        }

        override fun newArray(size: Int): Array<CameraPicker?> {
            return arrayOfNulls(size)
        }
    }
}