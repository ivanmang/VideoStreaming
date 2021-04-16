package com.example.myapplication

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import android.widget.RelativeLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;

class MainActivity : AppCompatActivity() {
    // Ask for Android device permissions at runtime.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_chat_view)

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel()
        }
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupLocalVideo()
        joinChannel()
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i(LOG_TAG, "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this,
                permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }


    private var mRtcEngine: RtcEngine? = null
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        // Listen for the onUserJoined callback.
        // This callback occurs when the remote user successfully joins the channel.
        // You can call the setupRemoteVideo method in this callback to set up the remote video view.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }

        // Listen for the onUserOffline callback.
        // This callback occurs when the remote user leaves the channel or drops offline.
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

    }

    // Initialize the RtcEngine object.
    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
        } catch (e: Exception) {
            Log.e(LOG_TAG, Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    private fun setupLocalVideo() {

        // Enable the video module.
        mRtcEngine!!.enableVideo()

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // Set the local video view.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {

        // Join a channel with a token.
        mRtcEngine!!.joinChannel(getString(R.string.agora_access_token), "test", "Extra Optional Data", 0)
    }


    // Kotlin
    // Listen for the onUserJoined callback.
    // This callback occurs when the remote user successfully joins the channel.
    // You can call the setupRemoteVideo method in this callback to set up the remote video view.
    // override fun onUserJoined(uid: Int, elapsed: Int) {
    //    runOnUiThread { setupRemoteVideo(uid) }
    //}


    private fun setupRemoteVideo(uid: Int) {
        val container1 = findViewById<RelativeLayout>(R.id.remote_video_view_container1)
        val container2 = findViewById<RelativeLayout>(R.id.remote_video_view_container2)

/*        if (container.childCount >= 1) {
            return
        }*/

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        val surfaceView2 = RtcEngine.CreateRendererView(baseContext)
        container1.addView(surfaceView)
        container2.addView(surfaceView2)

        // Set the remote video view.
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
        //mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView2, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun leaveChannel() {
        // Leave the current channel.
        mRtcEngine!!.leaveChannel()
    }

    private fun onRemoteUserLeft() {
        val container1 = findViewById<RelativeLayout>(R.id.remote_video_view_container1)
        val container2 = findViewById<RelativeLayout>(R.id.remote_video_view_container2)
        container1.removeAllViews()
        container2.removeAllViews()

        //val tipMsg = findViewById<TextView>(R.id.quick_tips_when_use_agora_sdk) // optional UI
        //tipMsg.visibility = View.VISIBLE
    }

    companion object {

        private val LOG_TAG = MainActivity::class.java.simpleName

        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}