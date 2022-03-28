package com.root.nativeCamera.ui.splash

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.root.nativeCamera.R
import com.root.nativeCamera.base.BaseActivity
import com.root.nativeCamera.ui.camera.CameraActivity
import com.livinglifetechway.quickpermissions_kotlin.runWithPermissions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsOptions
import com.livinglifetechway.quickpermissions_kotlin.util.QuickPermissionsRequest

class SplashActivity : BaseActivity() {

    private val quickPermissionsOption = QuickPermissionsOptions(
            handleRationale = true,
            rationaleMethod = { req -> permissionsPermanentlyDenied(req) },
            permanentDeniedMethod = { req -> permissionsPermanentlyDenied(req) }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        setUpFullScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        setUpUI()
    }

    private fun setUpUI() {
        Handler(Looper.getMainLooper()).postDelayed({
            runWithPermissions(
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.RECORD_AUDIO,
                    options = quickPermissionsOption
            ) {
                startActivity(Intent(this, CameraActivity::class.java))
                finish()
            }
        }, 3000)
    }

    private fun permissionsPermanentlyDenied(req: QuickPermissionsRequest) {
        runWithPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        ) {
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }
    }
}