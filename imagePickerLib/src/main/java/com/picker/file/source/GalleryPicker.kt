package com.picker.file.source

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.support.v4.app.Fragment
import com.picker.file.FilePickerConstants
import com.picker.file.PickerResult
import com.picker.file.exceptions.PermissionNotGrantedException
import com.picker.file.exceptions.RepeatRequiresPermissionException
import com.picker.file.extentions.inBackground
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.SingleOnSubscribe
import io.reactivex.functions.BiPredicate
import java.lang.IllegalStateException

class GalleryPicker() : BaseFilePicker() {

    val IMAGE_TYPE = "image/*"

    override fun pickFile(activity: Activity) {
        doPickOperation(activity)
    }

    override fun pickFile(fragment: Fragment) {
        doPickOperation(fragment)
    }

    override fun onRequestPermissionsResult(pickerContext: Any, requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            FilePickerConstants.REQUEST_CODE_STORAGE_PERMISSIONS -> {
                val isPermissionGranted = isRequestedPermissionGranted(permissions, grantResults, Manifest.permission.READ_EXTERNAL_STORAGE)
                if (isPermissionGranted) {
                    requestPick(pickerContext)
                } else {
                    resultSubject?.onError(PermissionNotGrantedException())
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK) {
            return
        }

        when (requestCode) {
            FilePickerConstants.REQUEST_CODE_GALLERY -> {
                data?.data?.let { filePath ->
                    resultSubject?.onNext(PickerResult(filePath))
                    resultSubject?.onComplete()
                } ?: run {
                    resultSubject?.onError(RuntimeException("There is no file result"))
                }
            }
        }
    }

    private fun doPickOperation(pickerContext: Any) {
        val permissionGranted = checkIfPermissionGranted(pickerContext, Manifest.permission.READ_EXTERNAL_STORAGE)

        if (permissionGranted) {
            requestPick(pickerContext)
        } else {
            requestPermission(pickerContext, FilePickerConstants.REQUEST_CODE_STORAGE_PERMISSIONS, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun requestPick(pickerContext: Any) {
        if (pickerContext is Activity) {
            pickerContext.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
        } else if (pickerContext is Fragment && pickerContext.context != null) {
            pickerContext.startActivityForResult(createIntent(), FilePickerConstants.REQUEST_CODE_GALLERY)
        } else {
            resultSubject?.onError(IllegalArgumentException("pickerContext could be only Activity or Fragment"))
        }
    }

    private fun createIntent(): Intent {
        return Intent.createChooser(Intent().setType(IMAGE_TYPE).setAction(Intent.ACTION_GET_CONTENT), "Image")
    }

    constructor(parcel: Parcel) : this()

    override fun writeToParcel(parcel: Parcel, flags: Int) {}

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<GalleryPicker> {
        override fun createFromParcel(parcel: Parcel): GalleryPicker {
            return GalleryPicker(parcel)
        }

        override fun newArray(size: Int): Array<GalleryPicker?> {
            return arrayOfNulls(size)
        }
    }
}