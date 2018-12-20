package com.picker.file.source

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.util.ArraySet
import java.lang.IllegalArgumentException

abstract class BaseFilePicker : FilePicker {

    val lifeCycleSet: MutableSet<LifeCycle> = ArraySet()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        lifeCycleSet.onEach { it.onActivityResult(requestCode, resultCode, data) }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        lifeCycleSet.onEach { it.onRequestPermissionsResult(requestCode, permissions, grantResults) }
    }

    @Throws(IllegalArgumentException::class)
    internal fun checkIfPermissionGranted(pickerContext: Any, permission: String): Boolean {
        var determineContext: Context? = null

        if (pickerContext is Activity) {
            determineContext = pickerContext
        }

        if (pickerContext is Fragment) {
            determineContext = pickerContext.context
        }

        return determineContext?.let { safeContext ->
            return ActivityCompat.checkSelfPermission(safeContext, permission) == PackageManager.PERMISSION_GRANTED
        } ?: run {
            throw IllegalArgumentException("Context is not determined")
        }
    }

    internal fun isRequestedPermissionGranted(permissions: Array<out String>, grantResults: IntArray, requestedPermission: String): Boolean {
        try {
            return grantResults[permissions.indexOf(requestedPermission)] == PackageManager.PERMISSION_GRANTED
        } catch (error: Throwable) {
            return false
        }
    }
}