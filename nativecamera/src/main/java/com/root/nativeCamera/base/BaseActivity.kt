package com.root.nativeCamera.base

import android.graphics.Color
import android.view.View
import androidx.appcompat.app.AppCompatActivity


open class BaseActivity : ScopedActivity() {

    companion object {
        /** Combination of all flags required to put activity into immersive mode */
        const val FLAGS_FULLSCREEN =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

        /** Milliseconds used for UI animations */
        const val ANIMATION_FAST_MILLIS = 10L
        const val ANIMATION_SLOW_MILLIS = 100L
        private const val IMMERSIVE_FLAG_TIMEOUT = 500L
    }


    fun showProgress(activity: AppCompatActivity) {
        MyProgress.show(activity)
    }

    fun hideProgress(activity: AppCompatActivity) {
        MyProgress.hide(activity)
    }

    fun setUpFullScreen() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.apply {
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }
}