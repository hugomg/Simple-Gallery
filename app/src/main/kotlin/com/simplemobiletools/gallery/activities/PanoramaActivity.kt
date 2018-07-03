package com.simplemobiletools.gallery.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
import com.google.vr.sdk.widgets.pano.VrPanoramaView
import com.simplemobiletools.commons.extensions.beVisible
import com.simplemobiletools.commons.extensions.toast
import com.simplemobiletools.commons.helpers.PERMISSION_WRITE_STORAGE
import com.simplemobiletools.gallery.R
import com.simplemobiletools.gallery.extensions.hideSystemUI
import com.simplemobiletools.gallery.extensions.navigationBarHeight
import com.simplemobiletools.gallery.extensions.showSystemUI
import com.simplemobiletools.gallery.helpers.PATH
import kotlinx.android.synthetic.main.activity_panorama.*

open class PanoramaActivity : SimpleActivity() {
    private var isFullScreen = true

    public override fun onCreate(savedInstanceState: Bundle?) {
        useDynamicTheme = false
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_panorama)
        supportActionBar?.hide()
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
        (cardboard.layoutParams as RelativeLayout.LayoutParams).bottomMargin = navigationBarHeight
        (explore.layoutParams as RelativeLayout.LayoutParams).bottomMargin = navigationBarHeight

        handlePermission(PERMISSION_WRITE_STORAGE) {
            if (it) {
                checkIntent()
            } else {
                toast(R.string.no_storage_permissions)
                finish()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        panorama_view.resumeRendering()
    }

    override fun onPause() {
        super.onPause()
        panorama_view.pauseRendering()
    }

    override fun onDestroy() {
        super.onDestroy()
        panorama_view.shutdown()
    }

    private fun checkIntent() {
        val path = intent.getStringExtra(PATH)
        intent.removeExtra(PATH)

        try {
            val options = VrPanoramaView.Options()
            options.inputType = VrPanoramaView.Options.TYPE_MONO
            Thread {
                val bitmap = BitmapFactory.decodeFile(path)
                runOnUiThread {
                    panorama_view.apply {
                        beVisible()
                        loadImageFromBitmap(bitmap, options)
                        setFlingingEnabled(true)
                        setPureTouchTracking(true)

                        // add custom buttons so we can position them and toggle visibility as desired
                        setFullscreenButtonEnabled(false)
                        setInfoButtonEnabled(false)
                        setTransitionViewEnabled(false)
                        setStereoModeButtonEnabled(false)

                        setOnClickListener {
                            handleClick()
                        }
                    }
                }
            }.start()
        } catch (e: Exception) {

        }

        window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            isFullScreen = visibility and View.SYSTEM_UI_FLAG_FULLSCREEN != 0
            toggleButtonVisibility()
        }
    }

    private fun toggleButtonVisibility() {
        cardboard.animate().alpha(if (isFullScreen) 0f else 1f)
        cardboard.isClickable = !isFullScreen

        explore.animate().alpha(if (isFullScreen) 0f else 1f)
        explore.isClickable = !isFullScreen
    }

    private fun handleClick() {
        isFullScreen = !isFullScreen
        toggleButtonVisibility()
        if (isFullScreen) {
            hideSystemUI(false)
        } else {
            showSystemUI(false)
        }
    }
}
