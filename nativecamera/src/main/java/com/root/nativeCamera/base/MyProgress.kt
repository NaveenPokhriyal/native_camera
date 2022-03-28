package com.root.nativeCamera.base

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import com.root.nativeCamera.R
import com.root.nativeCamera.databinding.LayoutProgressBinding


class MyProgress : DialogFragment() {

    lateinit var mBinding: LayoutProgressBinding

    companion object {

        var isShowing = false

        fun show(activity: AppCompatActivity) {
            if (isShowing) return
            val dialog = MyProgress()
            try {
                dialog.show(activity.supportFragmentManager, "_progress_dialog")
            } catch (e: Exception) {
                e.printStackTrace()
            }
            isShowing = true
        }

        fun hide(activity: AppCompatActivity) {
            val pD = activity.supportFragmentManager.findFragmentByTag("_progress_dialog")
            if (pD != null) {
                activity.supportFragmentManager.beginTransaction().remove(pD).commit()
                activity.supportFragmentManager.executePendingTransactions()
            }
            isShowing = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.MyProgressDialog);
        isCancelable = false
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.onCreateDialog(savedInstanceState)
        val builder = AlertDialog.Builder(requireContext())
        val lI =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        mBinding = LayoutProgressBinding.inflate(lI, null, false)
        builder.setView(mBinding.root)
        val aD = builder.create()
        resizeDialog(aD)
        return aD
    }

    private fun resizeDialog(dialog: Dialog?) {
        if (dialog != null) {
            val displayMetrics = DisplayMetrics()
            dialog.window?.apply {
                windowManager.defaultDisplay.getMetrics(displayMetrics)
                val layoutParams = WindowManager.LayoutParams()
                layoutParams.copyFrom(attributes)
                val dialogWindowWidth = (displayMetrics.widthPixels * 0.5f).toInt()
                val dialogWindowHeight = (displayMetrics.heightPixels * 0.5f).toInt()
                layoutParams.width = dialogWindowWidth
                layoutParams.height = dialogWindowHeight
                attributes = layoutParams
                setBackgroundDrawableResource(android.R.color.transparent);
                clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
        }
    }
}