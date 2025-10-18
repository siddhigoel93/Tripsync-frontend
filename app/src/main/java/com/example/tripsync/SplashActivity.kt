package com.example.tripsync

import android.content.Intent
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import kotlin.math.min

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_splash)

        val videoView = findViewById<VideoView>(R.id.splashVideo)
        val videoContainer = findViewById<FrameLayout>(R.id.video_container)

        val rawId = resources.getIdentifier("splash2", "raw", packageName)
        if (rawId == 0) {
            navigateToMain()
            return
        }

        val videoUri = Uri.parse("android.resource://$packageName/$rawId")
        videoView.setVideoURI(videoUri)

        val rotationDegrees = getVideoRotationDegrees(videoUri)

        videoView.setOnPreparedListener { mp ->
            mp.isLooping = false
            videoContainer.viewTreeObserver.addOnGlobalLayoutListener(object :
                ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    videoContainer.viewTreeObserver.removeOnGlobalLayoutListener(this)
                    val videoW = if (rotationDegrees == 90 || rotationDegrees == 270) mp.videoHeight else mp.videoWidth
                    val videoH = if (rotationDegrees == 90 || rotationDegrees == 270) mp.videoWidth else mp.videoHeight
                    sizeVideoToFitInside(videoView, videoContainer.width, videoContainer.height, videoW, videoH)
                    videoView.start()
                }
            })
        }

        videoView.setOnCompletionListener { navigateToMain() }
        videoView.setOnErrorListener { _, _, _ -> navigateToMain(); true }
    }

    private fun sizeVideoToFitInside(videoView: VideoView, containerW: Int, containerH: Int, videoW: Int, videoH: Int) {
        if (containerW <= 0 || containerH <= 0 || videoW <= 0 || videoH <= 0) return
        val scale = min(containerW.toFloat() / videoW.toFloat(), containerH.toFloat() / videoH.toFloat())
        val targetW = (videoW * scale).toInt()
        val targetH = (videoH * scale).toInt()
        val lp = FrameLayout.LayoutParams(targetW, targetH, Gravity.CENTER)
        videoView.layoutParams = lp
    }

    private fun getVideoRotationDegrees(uri: Uri): Int {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val degrees = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            retriever.release()
            degrees?.toIntOrNull() ?: 0
        } catch (_: Exception) {
            0
        }
    }

    private fun navigateToMain() {
        if (isFinishing) return
        startActivity(Intent(this@SplashActivity, MainActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        finish()
    }
}
