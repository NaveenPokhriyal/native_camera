package com.root.nativeCamera.util

import android.annotation.TargetApi
import android.content.Context
import android.graphics.Bitmap
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.os.Build
import androidx.core.content.ContextCompat.getSystemService
import com.example.couponsapp.base.ScopedActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*


fun ScopedActivity.savePicture(bitmap: Bitmap, file: File) = launch(Dispatchers.IO) {
    try {
        val out = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
        out.flush()
        out.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun ScopedActivity.createFile(baseFolder: File, format: String, extension: String) =
    File(
            baseFolder, SimpleDateFormat(format, Locale.US)
            .format(System.currentTimeMillis()) + extension
    )





