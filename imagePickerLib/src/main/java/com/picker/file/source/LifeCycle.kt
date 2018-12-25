package com.picker.file.source

import android.content.Intent

interface LifeCycle {

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)

    fun onRequestPermissionsResult(pickerContext: Any, requestCode: Int, permissions: Array<out String>, grantResults: IntArray)
}