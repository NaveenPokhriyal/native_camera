package com.root.nativeCamera.ui.preview

import android.graphics.Bitmap
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.root.nativeCamera.R
import com.root.nativeCamera.base.BaseActivity
import com.root.nativeCamera.databinding.ActivityPreViewBinding
import com.root.nativeCamera.filters.GPUImageBeautyFilter
import com.root.nativeCamera.util.ConstantObjects
import com.root.nativeCamera.util.SavePictureTask
import com.root.nativeCamera.util.createFile
import com.otaliastudios.cameraview.PictureResult
import jp.co.cyberagent.android.gpuimage.GPUImage
import jp.co.cyberagent.android.gpuimage.filter.GPUImageGrayscaleFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class PreViewActivity : BaseActivity(), SavePictureTask.OnPictureSaveListener {

    private lateinit var mBinding: ActivityPreViewBinding

    companion object {
        var pictureResult: PictureResult? = null
        var isBeautyFilterEnable: Boolean = false
        var isGrayScaleFilterEnable: Boolean = false
        var bitmap: Bitmap? = null
        var beautyLevel: Float = 0.0f
    }

    override fun onPause() {
        super.onPause()
        pictureResult = null
        isBeautyFilterEnable = false
        isGrayScaleFilterEnable = false
        beautyLevel = 0.0f
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpFullScreen()
        super.onCreate(savedInstanceState)
        setUpUI()
    }

    private fun clearGlide() {
        Glide.with(applicationContext).clear(mBinding.ivImage)
        Glide.get(applicationContext).bitmapPool.clearMemory()
    }

    private fun setUpUI() {
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_pre_view)
        clearGlide()
        val result = pictureResult ?: run {
            finish()
            return
        }

        result.toBitmap(result.size.width, result.size.height) {
            it?.let {
                val gpuImage = GPUImage(this)
                if (isBeautyFilterEnable) {
                    GPUImageBeautyFilter.beautyLevel = beautyLevel
                    gpuImage.setFilter(GPUImageBeautyFilter())
                    bitmap = gpuImage.getBitmapWithFilterApplied(it)
                    loadBitmap(bitmap!!)
                } else if (isGrayScaleFilterEnable) {
                    gpuImage.setFilter(GPUImageGrayscaleFilter())
                    bitmap = gpuImage.getBitmapWithFilterApplied(it)
                    loadBitmap(bitmap!!)
                } else {
                    bitmap = it
                    loadBitmap(bitmap!!)
                }
            }
        }

        mBinding.tvRetake.setOnClickListener {
            onBackPressed()
        }

        mBinding.tvSave.setOnClickListener {
            bitmap?.let {
                val file = createFile(
                        ConstantObjects.getOutputDirectory(this@PreViewActivity),
                        ConstantObjects.FILENAME,
                        ConstantObjects.PHOTO_EXTENSION
                )
                /*savePicture(it, file)*/
                showProgress(this@PreViewActivity)
                SavePictureTask(file, this@PreViewActivity).execute(it)
            }
        }
    }

    private fun loadBitmap(bitmap: Bitmap) {
        Glide.with(this@PreViewActivity)
                .load(bitmap)
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .transform()
                .into(mBinding.ivImage)
    }

    override fun onSaved(result: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            hideProgress(this@PreViewActivity)
            finish()
        }
    }

    override fun onError(error: String?) {
        GlobalScope.launch(Dispatchers.Main) {
            hideProgress(this@PreViewActivity)
            Toast.makeText(this@PreViewActivity, error, Toast.LENGTH_SHORT).show()
        }
    }
}