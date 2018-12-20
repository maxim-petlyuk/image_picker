package com.picker.picker.source

import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.util.ArraySet
import com.acorns.android.refactored.components.filePicker.source.FilePicker

abstract class BaseFilePicker : FilePicker {

    val lifeCycleSet: MutableSet<LifeCycle> = ArraySet()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        lifeCycleSet.onEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        lifeCycleSet.onEach { it.onRequestPermissionsResult(requestCode, permissions, grantResults) }
    }

    internal fun isPermissionGranted(permissions: Array<out String>, grantResults: IntArray, requestedPermission: String): Boolean {
        try {
            return grantResults[permissions.indexOf(requestedPermission)] == PackageManager.PERMISSION_GRANTED
        } catch (error: Throwable) {
            return false
        }
    }
}