package com.root.nativeCamera.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.os.Environment
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.transition.Slide
import androidx.transition.Transition
import androidx.transition.TransitionManager
import com.root.nativeCamera.R
import java.io.File

object ConstantObjects {
    const val FILENAME = "yyyy-MM-dd-HH-mm-ss-SSS"
    const val PHOTO_EXTENSION = ".jpg"
    const val VIDEO_EXTENSION = ".mp4"

    // slide the view from below itself to the current position
    fun slideUp(child: View, parent: ViewGroup) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 300
        transition.addTarget(child)
        TransitionManager.beginDelayedTransition(parent, transition)
        child.visibility = View.GONE
    }

    // slide the view from its current position to below itself
    fun slideDown(child: View, parent: ViewGroup) {
        val transition: Transition = Slide(Gravity.TOP)
        transition.duration = 300
        transition.addTarget(child)
        TransitionManager.beginDelayedTransition(parent, transition)
        child.visibility = View.VISIBLE
    }

    /** Use external media if it is available, our app's file directory otherwise */
    fun getOutputDirectory(context: Context): File {
        val appContext = context.applicationContext
        val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
            File(it, appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists())
            mediaDir else appContext.filesDir
    }

    fun getVideoOutputDirectory(context: Context): File {
        return File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera")
    }

    fun rotateImage(galleryBitmap: Bitmap, file: File): Bitmap? {
        var ei: ExifInterface? = null
        try {
            ei = ExifInterface(file.absolutePath)
        } catch (e: Exception) {
        }
        val orientation = ei!!.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_UNDEFINED
        )
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotatePicture(galleryBitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotatePicture(galleryBitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotatePicture(galleryBitmap, 270f)
            ExifInterface.ORIENTATION_NORMAL -> galleryBitmap
            else -> galleryBitmap
        }
    }

    fun rotatePicture(source: Bitmap, angle: Float): Bitmap? {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(
                source, 0, 0, source.width, source.height,
                matrix, true
        )
    }

    fun getFileName(): String {
        return "VVish_${System.currentTimeMillis()}.JPEG"
    }

}