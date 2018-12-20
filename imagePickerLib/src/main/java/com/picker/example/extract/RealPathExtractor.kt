package com.picker.picker.extract

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.content.CursorLoader
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.TextUtils

class RealPathExtractor {

    fun getRealPath(context: Context, fileUri: Uri): PickedFile? {
        return getRealPathFromURI_API19(context, fileUri)
    }

    @SuppressLint("NewApi")
    private fun getRealPathFromURI_API11to18(context: Context, contentUri: Uri): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        var result: String? = null

        val cursorLoader = CursorLoader(context, contentUri, proj, null, null, null)
        val cursor = cursorLoader.loadInBackground()

        if (cursor != null) {
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            result = cursor.getString(column_index)
            cursor.close()
        }

        return result
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    @SuppressLint("NewApi")
    private fun getRealPathFromURI_API19(context: Context, uri: Uri): PickedFile? {
        val isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                if ("primary".equals(type, ignoreCase = true)) {
                    return LocalFile(Environment.getExternalStorageDirectory().toString() + "/" + split[1])
                }
            } else if (isDownloadsDocument(uri)) {
                val id = DocumentsContract.getDocumentId(uri)

                if (!TextUtils.isEmpty(id)) {
                    if (id.startsWith("raw:")) {
                        return LocalFile(id.replaceFirst("raw:".toRegex(), ""))
                    }

                    try {
                        val contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), java.lang.Long.valueOf(id))
                        return getDataColumn(context, contentUri, null, null)
                    } catch (error: NumberFormatException) {
                        return null
                    }

                }
            } else if (isMediaDocument(uri)) {
                val docId = DocumentsContract.getDocumentId(uri)
                val split = docId.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                val type = split[0]

                var contentUri: Uri? = null
                if ("image" == type) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                } else if ("video" == type) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                } else if ("audio" == type) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
                }

                val selection = "_id=?"
                val selectionArgs = arrayOf(split[1])
                return getDataColumn(context, contentUri, selection, selectionArgs)
            } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
                return getDataColumn(context, uri, null, null)
            }// MediaProvider
            // DownloadsProvider
        } else if ("content".equals(uri.scheme!!, ignoreCase = true)) {
            // Return the remote address
            return if (isGooglePhotosUri(uri))
                LocalFile(uri.lastPathSegment)
            else
                getDataColumn(context, uri, null, null)
        } else if ("file".equals(uri.scheme!!, ignoreCase = true)) {
            return LocalFile(uri.path)
        }// File
        // MediaStore (and general)

        return null
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private fun getDataColumn(context: Context, uri: Uri?, selection: String?,
                              selectionArgs: Array<String>?): PickedFile? {

        var cursor: Cursor? = null
        val projection = arrayOf(MediaStore.MediaColumns.DATA, MediaStore.MediaColumns.DISPLAY_NAME)

        try {
            cursor = context.contentResolver.query(uri!!, projection, selection, selectionArgs, null)

            if (cursor != null && cursor.moveToFirst()) {
                // Fall back to writing to file if _data column does not exist
                val index = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)
                val path = if (index > -1) cursor.getString(index) else null

                if (path != null) {
                    return LocalFile(cursor.getString(index))
                } else {
                    val indexDisplayName = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                    val fileName = cursor.getString(indexDisplayName)
                    return RemoteFile(uri, fileName)
                }
            }
        } finally {
            cursor?.close()
        }

        return null
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private fun isExternalStorageDocument(uri: Uri): Boolean {
        return "com.android.externalstorage.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private fun isDownloadsDocument(uri: Uri): Boolean {
        return "com.android.providers.downloads.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private fun isMediaDocument(uri: Uri): Boolean {
        return "com.android.providers.media.documents" == uri.authority
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private fun isGooglePhotosUri(uri: Uri): Boolean {
        return "com.google.android.apps.photos.content" == uri.authority
    }
}
