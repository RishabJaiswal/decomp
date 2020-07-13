package com.decomp.comp.decomp.application

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.utils.extensions.getColor

abstract class BaseActivity : AppCompatActivity() {

    private var baseAlertDialog: AlertDialog? = null
    private val inputManager by lazy {
        getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeStatusBarColor(R.color.colorPrimaryDark)
        if (isScreenCaptureDisabled()) {
            disableScreenCapture()
        }
    }

    override fun onDestroy() {
        baseAlertDialog?.dismiss()
        super.onDestroy()
    }

    fun changeStatusBarColor(@ColorRes statusBarColor: Int) {
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = statusBarColor.getColor(this)
    }

    protected open fun isScreenCaptureDisabled(): Boolean {
        return false
    }

    protected fun disableScreenCapture() {
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }

    protected fun showKeyboard() {
        currentFocus?.let { view ->
            inputManager.showSoftInput(view, 0)
        }
    }

    protected fun hideKeyboard() {
        currentFocus?.windowToken?.let { token ->
            inputManager.hideSoftInputFromWindow(token, 0)
        }
    }

    protected fun showAlertDialog(
            title: String,
            message: String,
            positiveLabel: String,
            negativeLabel: String?,
            onPositiveAction: (() -> Unit)? = null,
            onNegativeAction: (() -> Unit)? = null
    ) {
        if (baseAlertDialog == null) {
            val dialogBuilder = AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveLabel) { dialog, _ ->
                        onPositiveAction?.invoke() ?: dialog.dismiss()
                    }
            negativeLabel?.let {
                dialogBuilder.setNegativeButton(it) { dialog, _ ->
                    onNegativeAction?.invoke() ?: dialog.dismiss()
                }
            }
            baseAlertDialog = dialogBuilder.create()
        } else {
            baseAlertDialog?.apply {
                dismiss()
                setTitle(title)
                setMessage(message)

                //setting positive button
                setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        positiveLabel
                ) { _, _ ->
                    onPositiveAction?.invoke()
                }

                //setting negative button
                setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        negativeLabel
                ) { _, _ ->
                    onNegativeAction?.invoke()
                }
            }
        }
        baseAlertDialog?.show()
    }

    protected fun showAlertDialog(
            title: Int,
            message: Int,
            positiveLabel: Int,
            negativeLabel: Int? = null,
            onPositiveAction: (() -> Unit)? = null,
            onNegativeAction: (() -> Unit)? = null
    ) {
        if (baseAlertDialog == null) {
            val dialogBuilder = AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton(positiveLabel) { dialog, _ ->
                        onPositiveAction?.invoke() ?: dialog.dismiss()
                    }
            negativeLabel?.let {
                dialogBuilder.setNegativeButton(it) { dialog, _ ->
                    onNegativeAction?.invoke() ?: dialog.dismiss()
                }
            }
            baseAlertDialog = dialogBuilder.create()
        } else {
            baseAlertDialog?.apply {
                dismiss()
                setTitle(title)
                setMessage(getString(message))

                //setting positive button
                setButton(
                        AlertDialog.BUTTON_POSITIVE,
                        getString(positiveLabel)
                ) { _, _ ->
                    onPositiveAction?.invoke()
                }

                //setting negative button
                val negativeLblString = negativeLabel?.let {
                    getString(it)
                } ?: ""

                setButton(
                        AlertDialog.BUTTON_NEGATIVE,
                        negativeLblString
                ) { _, _ ->
                    onNegativeAction?.invoke()
                }
            }
        }
        baseAlertDialog?.show()
    }
}