package com.root.nativeCamera.base

import android.app.Activity
import android.app.Application
import com.root.nativeCamera.R
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule

class BaseApplication : Application(), KodeinAware {

    private var mCurrentActivity: Activity? = null

    override val kodein: Kodein = Kodein.lazy {
        import(androidXModule(this@BaseApplication))
    }

    fun getCurrentActivity(): Activity? {
        return mCurrentActivity
    }

    fun setCurrentActivity(mCurrentActivity: Activity?) {
        this.mCurrentActivity = mCurrentActivity
    }


    companion object {
        private lateinit var instance: BaseApplication
        fun get(): BaseApplication = instance
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        setUpFont()
    }

    private fun setUpFont() {
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/montserrat_bold.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )
    }
}