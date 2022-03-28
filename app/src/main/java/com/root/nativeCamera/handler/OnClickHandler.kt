package com.root.nativeCamera.handler

import android.view.View

interface OnClickHandler {
    fun onFlashUtilClick(view: View)
    fun onEffectUtilClick(view: View)
    fun onSelectVideo(view: View)
    fun onSelectCamera(view: View)
    fun onSelectGallery(view: View)
    fun onRotateCamera(view: View)
    fun onImageCapture(view: View)
    fun onBeautyEffectSelect(view: View)
    fun onGreyScaleFilterSelect(view: View)
    fun onFlashOnClick(view: View)
    fun onFlashOffClick(view: View)
    fun onFlashAutoClick(view: View)
    fun onTorchOnClick(view: View)
}