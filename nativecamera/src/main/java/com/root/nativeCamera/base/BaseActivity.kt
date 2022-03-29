package com.root.nativeCamera.base

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import com.example.couponsapp.base.ScopedActivity
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.closestKodein
import org.kodein.di.generic.instance


open class BaseActivity : ScopedActivity(), KodeinAware {
    override val kodein: Kodein by closestKodein()
//    private lateinit var mMyApp: BaseApplication
    private val baseViewModel: BaseViewModel by instance()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        mMyApp = this.applicationContext as BaseApplication
    }

    fun showProgress(activity: AppCompatActivity) {
        MyProgress.show(activity)
    }

    fun hideProgress(activity: AppCompatActivity) {
        MyProgress.hide(activity)
    }

    override fun onResume() {
        super.onResume()
//        mMyApp.setCurrentActivity(this)
    }

    override fun onPause() {
        super.onPause()
        clearReferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        clearReferences()
    }

    private fun clearReferences() {
//        val currActivity: Activity? = mMyApp.getCurrentActivity()
//        if (this == currActivity) mMyApp.setCurrentActivity(null)
    }

    fun setUpFullScreen() {
//        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.apply {
            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = Color.TRANSPARENT
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }
}