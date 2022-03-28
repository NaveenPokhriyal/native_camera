package com.root.nativeCamera.ui.camera

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.media.MediaActionSound
import android.media.MediaScannerConnection
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import com.root.nativeCamera.R
import com.root.nativeCamera.base.BaseActivity
import com.root.nativeCamera.databinding.ActivityCameraBinding
import com.root.nativeCamera.filters.BeautyFilter
import com.root.nativeCamera.filters.GPUImageBeautyFilter
import com.root.nativeCamera.handler.OnClickHandler
import com.root.nativeCamera.ui.preview.PreViewActivity
import com.root.nativeCamera.util.ConstantObjects
import com.root.nativeCamera.util.ConstantObjects.FILENAME
import com.root.nativeCamera.util.ConstantObjects.PHOTO_EXTENSION
import com.root.nativeCamera.util.ConstantObjects.VIDEO_EXTENSION
import com.root.nativeCamera.util.ConstantObjects.getOutputDirectory
import com.root.nativeCamera.util.OnSwipeTouchListener
import com.root.nativeCamera.util.SavePictureTask
import com.root.nativeCamera.util.createFile
import com.otaliastudios.cameraview.*
import com.otaliastudios.cameraview.controls.*
import com.otaliastudios.cameraview.filter.Filters
import com.otaliastudios.cameraview.filter.MultiFilter
import com.otaliastudios.cameraview.filters.GrayscaleFilter
import com.otaliastudios.cameraview.gesture.Gesture
import com.otaliastudios.cameraview.gesture.GestureAction
import com.otaliastudios.cameraview.size.AspectRatio
import com.otaliastudios.cameraview.size.SizeSelector
import com.otaliastudios.cameraview.size.SizeSelectors
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class CameraActivity : BaseActivity(), OnClickHandler, SavePictureTask.OnPictureSaveListener {

    private var camera: CameraView? = null
    private var beautyFilter: BeautyFilter? = null
    private var captureSound: MediaActionSound? = null
    private var pictureFlashMode: Flash = Flash.AUTO
    private var videoFlashMode: Flash = Flash.OFF
    private var facing: Facing = Facing.BACK
    private var blackAndWhiteFilter: GrayscaleFilter? = null
    private lateinit var mBinding: ActivityCameraBinding

    //timer
    private var timeInterval: Long = 1000
    private var timeInMillis: Long = 0
    private var timer: Runnable = getTimer()
    private var timerHandler: Handler? = null

    private fun getTimer(): Runnable {
        return Runnable {
            mBinding.tvTimer.text = getTime(timeInMillis)
            timeInMillis++
            timerHandler?.postDelayed(timer, timeInterval)
        }
    }

    companion object {
        const val ANIMATION_FAST_MILLIS = 50L
    }

    override fun onPause() {
        super.onPause()
        camera?.let {
            if (it.isTakingVideo) {
                it.stopVideo()
            } else {
                applyFilter()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpFullScreen()
        super.onCreate(savedInstanceState)
        setUpUi()
    }

    private fun setUpUi() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_camera)
        mBinding.onClickHandler = this@CameraActivity
        beautyFilter = BeautyFilter()
        blackAndWhiteFilter = GrayscaleFilter()
        captureSound = MediaActionSound()
        timerHandler = Handler(mainLooper)
        // Build UI controls
        startCamera()
        //set beauty controller
        setBeautyController()
    }

    private fun setBeautyController() {
        mBinding.sbBeautyLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, p2: Boolean) {
                val totalProgress = 100 - progress
                val level: Float = String.format("%.2f", (totalProgress.toFloat() / 30)).toFloat()
                beautyFilter?.beautyLevel = level
                applyBeautyFilter(level)
            }

            override fun onStartTrackingTouch(p0: SeekBar?) {
                //do noting
            }

            override fun onStopTrackingTouch(p0: SeekBar?) {
                //do noting
            }
        })
    }

    private fun applyBeautyFilter(level: Float) {
        if (mBinding.ivBeautyEffect.isSelected && mBinding.ivBlackAndWhiteEffect.isSelected) {
            if (level > 3.0f) {
                camera?.filter = blackAndWhiteFilter!!
            } else {
                camera?.filter = MultiFilter(beautyFilter, blackAndWhiteFilter)
            }
        } else if (mBinding.ivBeautyEffect.isSelected) {
            if (level > 3.0f) {
                camera?.filter = Filters.NONE.newInstance()
            } else {
                camera?.filter = beautyFilter!!
            }
        }
    }

    private fun startCamera() {
        //camera view..
        camera = mBinding.camera
        camera?.setLifecycleOwner(this)
        camera?.addCameraListener(Listener())
        camera?.pictureMetering = true
        camera?.useDeviceOrientation = true
        camera?.previewFrameRate = 7f
        camera?.whiteBalance = WhiteBalance.AUTO
        camera?.startAutoFocus((camera!!.width / 2F), (camera!!.height / 2F))
        camera?.mapGesture(Gesture.TAP, GestureAction.AUTO_FOCUS)
        val width: SizeSelector = SizeSelectors.minWidth(1080)
        val height: SizeSelector = SizeSelectors.minHeight(1920)
        val dimensions: SizeSelector = SizeSelectors.and(width, height)
        val ratio = SizeSelectors.aspectRatio(AspectRatio.of(9, 16), 0f) // Matches 1:1 sizes.
        val result = SizeSelectors.and(ratio, dimensions)
        camera?.setPictureSize(result)
        mBinding.ivFlashAuto.isSelected = true
        mBinding.cbImageSelector.isSelected = true
        pictureFlashMode = Flash.AUTO
        setFlash()
        setGestureListener()
        Toast.makeText(this@CameraActivity, getString(R.string.msg_adjust_brightness), Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setGestureListener() {
        mBinding.camera.setOnTouchListener(object : OnSwipeTouchListener(this@CameraActivity) {
            override fun onSwipeRight() {
                super.onSwipeRight()
                if (mBinding.cbImageSelector.isSelected) {
                    onSelectVideo(mBinding.cbVideoSelector)
                } else if (mBinding.cbGallerySelector.isSelected) {
                    onSelectCamera(mBinding.cbImageSelector)
                }
            }

            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if (mBinding.cbVideoSelector.isSelected) {
                    onSelectCamera(mBinding.cbImageSelector)
                } else if (mBinding.cbImageSelector.isSelected) {
                    onSelectGallery(mBinding.cbGallerySelector)
                }
            }

            override fun onZoom(zoomLevel: Float) {
                super.onZoom(zoomLevel)
                camera?.zoom = zoomLevel
            }
        })
    }

    private fun recordVideo() {
        val videoFile = createFile(getOutputDirectory(this@CameraActivity), FILENAME, VIDEO_EXTENSION)
        camera?.takeVideo(videoFile,10000)
        startTimer()
        mBinding.ivRotateCamera.visibility = View.INVISIBLE
    }

    private fun startTimer() {
        timerHandler?.postDelayed(timer, timeInterval)
    }

    /** Performs recording animation of flashing screen */
    private val animationTask: Runnable by lazy {
        Runnable {
            // Flash white animation
            mBinding.overlay.background = Color.argb(150, 255, 255, 255).toDrawable()
            // Wait for ANIMATION_FAST_MILLIS
            mBinding.overlay.postDelayed({
                // Remove white flash animation
                mBinding.overlay.background = null
            }, ANIMATION_FAST_MILLIS)
        }
    }

    private fun getTime(milliseconds: Long): String {
        val minutes = (milliseconds) / 60
        val seconds = (milliseconds) % 60
        return if (seconds.toString().length == 1)
            "0$minutes:0$seconds"
        else
            "0$minutes:$seconds"
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setFlash() {
        if (mBinding.cbVideoSelector.isSelected) {
            camera?.flash = videoFlashMode
            when (videoFlashMode) {
                Flash.OFF -> {
                    mBinding.ivFlashOff.isSelected = true
                    mBinding.ivFlashOn.isSelected = false
                    mBinding.ivFlashAuto.isSelected = false
                    mBinding.ivTorch.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_off))
                }
                Flash.ON -> {
                    mBinding.ivFlashOn.isSelected = true
                    mBinding.ivFlashOff.isSelected = false
                    mBinding.ivFlashAuto.isSelected = false
                    mBinding.ivTorch.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_selected))
                }
                else -> {}
            }
        } else {
            camera?.flash = pictureFlashMode
            when (pictureFlashMode) {
                Flash.OFF -> {
                    mBinding.ivFlashOff.isSelected = true
                    mBinding.ivFlashOn.isSelected = false
                    mBinding.ivFlashAuto.isSelected = false
                    mBinding.ivTorch.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_off))
                }
                Flash.ON -> {
                    mBinding.ivFlashOn.isSelected = true
                    mBinding.ivFlashOff.isSelected = false
                    mBinding.ivFlashAuto.isSelected = false
                    mBinding.ivTorch.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_selected))
                }
                Flash.TORCH -> {
                    mBinding.ivTorch.isSelected = true
                    mBinding.ivFlashOff.isSelected = false
                    mBinding.ivFlashOn.isSelected = false
                    mBinding.ivFlashAuto.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_torch))
                }
                Flash.AUTO -> {
                    mBinding.ivFlashAuto.isSelected = true
                    mBinding.ivFlashOn.isSelected = false
                    mBinding.ivFlashOff.isSelected = false
                    mBinding.ivTorch.isSelected = false
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mBinding.ivCapture.isClickable = true
        setFlash()
    }

    override fun onFlashUtilClick(view: View) {
        if (mBinding.ivLeftMenu.isSelected) {
            mBinding.ivLeftMenu.isSelected = false
            ConstantObjects.slideUp(
                    mBinding.clChildLeftMenuContainer,
                    mBinding.clParentLeftMenuContainer
            )
        } else {
            mBinding.ivLeftMenu.isSelected = true
            ConstantObjects.slideDown(
                    mBinding.clChildLeftMenuContainer,
                    mBinding.clParentLeftMenuContainer
            )
        }
    }

    override fun onEffectUtilClick(view: View) {
        if (mBinding.ivMenuRight.isSelected) {
            mBinding.ivMenuRight.isSelected = false
            ConstantObjects.slideUp(
                    mBinding.clChildRightMenuContainer,
                    mBinding.clParentRightMenuContainer
            )
        } else {
            mBinding.ivMenuRight.isSelected = true
            ConstantObjects.slideDown(
                    mBinding.clChildRightMenuContainer,
                    mBinding.clParentRightMenuContainer
            )
        }
    }

    override fun onSelectVideo(view: View) {
        disableMultitap(view)
        camera?.engine = Engine.CAMERA1
        camera?.mode = Mode.VIDEO
        camera?.preview = Preview.TEXTURE
        camera?.videoCodec = VideoCodec.DEVICE_DEFAULT
        camera?.audioCodec = AudioCodec.DEVICE_DEFAULT
        camera?.audio = Audio.ON
        mBinding.ivFlashAuto.visibility = View.GONE
        mBinding.ivTorch.visibility = View.GONE
        mBinding.ivBeautyEffect.visibility = View.GONE
        mBinding.ivBlackAndWhiteEffect.visibility = View.GONE
        mBinding.ivMenuRight.visibility = View.GONE
        mBinding.cbVideoSelector.isSelected = true
        mBinding.cbGallerySelector.isSelected = false
        mBinding.cbImageSelector.isSelected = false
        mBinding.clVideoTimerContainer.visibility = View.VISIBLE
        mBinding.clCameraFilterContainer.visibility = View.GONE
        setFlash()
        applyFilter()
    }

    private fun disableMultitap(view: View) {
        view.isEnabled = false
        view.isClickable = false
        Handler(mainLooper).postDelayed({
            view.isEnabled = true
            view.isClickable = true
        }, 1500)
    }

    override fun onSelectCamera(view: View) {
        if (!camera!!.isTakingVideo) {
            disableMultitap(view)
            camera?.mode = Mode.PICTURE
            camera?.preview = Preview.GL_SURFACE
            camera?.engine = Engine.CAMERA1
            mBinding.ivFlashAuto.visibility = View.VISIBLE
            mBinding.ivTorch.visibility = View.VISIBLE
            mBinding.ivMenuRight.visibility = View.VISIBLE
            mBinding.ivBeautyEffect.visibility = View.VISIBLE
            mBinding.ivBlackAndWhiteEffect.visibility = View.VISIBLE
            mBinding.cbVideoSelector.isSelected = false
            mBinding.cbGallerySelector.isSelected = false
            mBinding.cbImageSelector.isSelected = true
            mBinding.clVideoTimerContainer.visibility = View.GONE
            mBinding.clCameraFilterContainer.visibility = View.GONE
            if (mBinding.ivBeautyEffect.isSelected) {
                mBinding.clCameraFilterContainer.visibility = View.VISIBLE
            } else {
                mBinding.clCameraFilterContainer.visibility = View.GONE
            }
            setFlash()
            applyFilter()
        }
    }

    override fun onSelectGallery(view: View) {
        if (!camera!!.isTakingVideo) {
            disableMultitap(view)
            camera?.mode = Mode.PICTURE
            camera?.preview = Preview.GL_SURFACE
            camera?.engine = Engine.CAMERA1
            mBinding.ivFlashAuto.visibility = View.VISIBLE
            mBinding.ivTorch.visibility = View.VISIBLE
            mBinding.ivMenuRight.visibility = View.VISIBLE
            mBinding.ivBeautyEffect.visibility = View.VISIBLE
            mBinding.ivBlackAndWhiteEffect.visibility = View.VISIBLE
            mBinding.cbVideoSelector.isSelected = false
            mBinding.cbGallerySelector.isSelected = true
            mBinding.cbImageSelector.isSelected = false
            mBinding.clVideoTimerContainer.visibility = View.GONE
            mBinding.clCameraFilterContainer.visibility = View.GONE
            if (mBinding.ivBeautyEffect.isSelected) {
                mBinding.clCameraFilterContainer.visibility = View.VISIBLE
            } else {
                mBinding.clCameraFilterContainer.visibility = View.GONE
            }
            setFlash()
            applyFilter()
        }
    }

    override fun onRotateCamera(view: View) {
        disableMultitap(view)
        facing = if (facing == Facing.BACK) {
            Facing.FRONT
        } else {
            Facing.BACK
        }
        camera?.facing = facing
        setFlash()
    }

    override fun onImageCapture(view: View) {
        disableMultitap(view)
        setFlash()
        camera?.let {
            if (it.mode == Mode.VIDEO) {
                if (it.isTakingVideo) {
                    it.stopVideo()
                } else {
                    recordVideo()
                }
            } else {
                if (!it.isTakingPicture)
                    it.takePicture()
            }
            camera?.post(animationTask)
        }
    }

    override fun onBeautyEffectSelect(view: View) {
        mBinding.ivBlackAndWhiteEffect.isSelected = false
        mBinding.clVideoTimerContainer.visibility = View.GONE
        view.isSelected = !view.isSelected
        mBinding.sbBeautyLevel.progress = 50
        val level: Float = String.format("%.2f", (50.0 / 30)).toFloat()
        beautyFilter?.beautyLevel = level
        if (view.isSelected) {
            mBinding.clCameraFilterContainer.visibility = View.VISIBLE
        } else {
            mBinding.clCameraFilterContainer.visibility = View.GONE
        }
        applyFilter()
    }

    override fun onGreyScaleFilterSelect(view: View) {
        mBinding.ivBeautyEffect.isSelected = false
        mBinding.clCameraFilterContainer.visibility = View.GONE
        mBinding.clVideoTimerContainer.visibility = View.GONE
        view.isSelected = !view.isSelected
        applyFilter()
    }

    private fun applyFilter() {
        if (!cbVideoSelector.isSelected) {
            if (mBinding.ivBlackAndWhiteEffect.isSelected && mBinding.ivBeautyEffect.isSelected) {
                camera?.filter = MultiFilter(beautyFilter, blackAndWhiteFilter)
            } else if (mBinding.ivBlackAndWhiteEffect.isSelected && !mBinding.ivBeautyEffect.isSelected) {
                camera?.filter = blackAndWhiteFilter!!
            } else if (!mBinding.ivBlackAndWhiteEffect.isSelected && mBinding.ivBeautyEffect.isSelected) {
                camera?.filter = beautyFilter!!
            } else {
                camera?.filter = Filters.NONE.newInstance()
            }
        } else {
            camera?.filter = Filters.NONE.newInstance()
        }
    }

    private fun isAnyOptionSelected(): Boolean {
        return mBinding.ivFlashOn.isSelected || mBinding.ivFlashOff.isSelected || mBinding.ivFlashAuto.isSelected || mBinding.ivTorch.isSelected
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onFlashOnClick(view: View) {
        view.isSelected = !view.isSelected
        if (mBinding.cbVideoSelector.isSelected) {
            if (view.isSelected) {
                videoFlashMode = Flash.ON
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_selected))
                setFlash()
            } else {
                videoFlashMode = Flash.OFF
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_off))
                setFlash()
            }
        } else {
            if (view.isSelected) {
                pictureFlashMode = Flash.ON
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_selected))
                setFlash()
            } else {
                if (!isAnyOptionSelected()) {
                    pictureFlashMode = Flash.OFF
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
                    setFlash()
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onFlashOffClick(view: View) {
        if (mBinding.cbVideoSelector.isSelected) {
            videoFlashMode = Flash.OFF
            mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_off))
            setFlash()
        } else {
            view.isSelected = !view.isSelected
            if (view.isSelected) {
                pictureFlashMode = Flash.OFF
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_flash_off))
                setFlash()
            } else {
                if (!isAnyOptionSelected()) {
                    pictureFlashMode = Flash.AUTO
                    mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
                    setFlash()
                }
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onFlashAutoClick(view: View) {
        view.isSelected = !view.isSelected
        if (view.isSelected) {
            pictureFlashMode = Flash.AUTO
            mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
            setFlash()
        } else {
            if (!isAnyOptionSelected()) {
                mBinding.ivFlashAuto.isSelected = true
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
                setFlash()
            }
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onTorchOnClick(view: View) {
        view.isSelected = !view.isSelected
        if (view.isSelected) {
            pictureFlashMode = Flash.TORCH
            mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_torch))
            setFlash()
        } else {
            if (!isAnyOptionSelected()) {
                pictureFlashMode = Flash.AUTO
                mBinding.ivMenuLeft.setImageDrawable(getDrawable(R.drawable.ic_left_menu_auto_flash))
                setFlash()
            }
        }
    }

    private inner class Listener : CameraListener() {

        override fun onCameraOpened(options: CameraOptions) {

        }

        override fun onCameraError(exception: CameraException) {
            super.onCameraError(exception)
            exception.printStackTrace()
        }

        override fun onPictureTaken(result: PictureResult) {
            super.onPictureTaken(result)
            camera?.startAutoFocus((camera!!.width / 2F), (camera!!.height / 2F))
            if (mBinding.cbImageSelector.isSelected || mBinding.cbVideoSelector.isSelected) {
                PreViewActivity.isBeautyFilterEnable = mBinding.ivBeautyEffect.isSelected
                beautyFilter?.let {
                    PreViewActivity.beautyLevel = it.beautyLevel
                }
                PreViewActivity.isGrayScaleFilterEnable = mBinding.ivBlackAndWhiteEffect.isSelected
                PreViewActivity.pictureResult = result
                startActivity(Intent(this@CameraActivity, PreViewActivity::class.java))
            } else {
                val gpuImage = GPUImage(this@CameraActivity)
                result.toBitmap(result.size.width, result.size.height) {
                    it?.let {
                        val file = createFile(
                                getOutputDirectory(this@CameraActivity),
                                FILENAME,
                                PHOTO_EXTENSION
                        )
                        if (mBinding.ivBeautyEffect.isSelected) {
                            GPUImageBeautyFilter.beautyLevel = beautyFilter?.beautyLevel!!
                            gpuImage.setFilter(GPUImageBeautyFilter())
                            showProgress(this@CameraActivity)
                            SavePictureTask(file, this@CameraActivity).execute(gpuImage.getBitmapWithFilterApplied(it))
                        } else if (mBinding.ivBlackAndWhiteEffect.isSelected) {
                            gpuImage.setFilter(GPUImageGrayscaleFilter())
                            showProgress(this@CameraActivity)
                            SavePictureTask(file, this@CameraActivity).execute(gpuImage.getBitmapWithFilterApplied(it))
                        } else {
                            showProgress(this@CameraActivity)
                            SavePictureTask(file, this@CameraActivity).execute(it)
                        }
                    }
                }
            }
        }

        override fun onVideoTaken(result: VideoResult) {
            super.onVideoTaken(result)
            if (result.file.exists()) {
                Toast.makeText(this@CameraActivity, getString(R.string.msg_saving_file), Toast.LENGTH_SHORT).show()
                MediaScannerConnection.scanFile(this@CameraActivity, arrayOf(result.file.absolutePath), null, null)
            }
        }

        override fun onVideoRecordingStart() {
            super.onVideoRecordingStart()
            if (videoFlashMode == Flash.ON) {
                camera?.flash = Flash.TORCH
            }
        }

        override fun onVideoRecordingEnd() {
            super.onVideoRecordingEnd()
            if (videoFlashMode == Flash.ON) {
                camera?.flash = Flash.ON
            }
            timeInMillis = 0
            mBinding.tvTimer.text = getTime(timeInMillis)
            timerHandler?.removeCallbacks(timer)
            mBinding.ivRotateCamera.visibility = View.VISIBLE
        }
    }

    override fun onSaved(result: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            hideProgress(this@CameraActivity)
        }
    }

    override fun onError(error: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            hideProgress(this@CameraActivity)
            Toast.makeText(this@CameraActivity, error, Toast.LENGTH_SHORT).show()
        }
    }
}