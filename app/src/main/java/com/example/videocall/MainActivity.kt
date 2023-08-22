package com.example.videocall

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.videocall.databinding.ActivityMainBinding
import com.example.videocall.media.RtcTokenBuilder2
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val PERMISSION_REQUEST = 22

    private val  listOfPermission = arrayOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.RECORD_AUDIO
    )


    private val appId = "your app Id"
    private var channelName: String? = null
    private var token: String? =  null
    private val uId = 0
    private val appCertificate = "your app certificate"
    private var isJoined = false
    private var agoraEngine : RtcEngine? = null
    private var localView : SurfaceView? = null
    private var remoteView : SurfaceView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val tokenBuilder = RtcTokenBuilder2()
        channelName = intent?.getStringExtra("channelName")
        token = tokenBuilder.buildTokenWithUid(appId, appCertificate, channelName, uId,RtcTokenBuilder2.Role.ROLE_PUBLISHER, 1000000, 100000)
        Log.d("tokenGenerated", token.toString())
        Log.d("channelName", channelName.toString())




        if (!checkPermission()){
            ActivityCompat.requestPermissions(
                this,
                listOfPermission,
                PERMISSION_REQUEST
            )
        }
        setUpVideoSdkEngine()
        joinCall()



        binding.LeaveButton.setOnClickListener {
            leaveCall()
        }




    }



    override fun onDestroy() {
        super.onDestroy()
        agoraEngine!!.stopPreview()
        agoraEngine!!.leaveChannel()

        Thread{
            RtcEngine.destroy()
            agoraEngine = null
        }.start()
    }


    private fun setUpVideoSdkEngine(){
        try {
            val rtcEngineConfig = RtcEngineConfig()
            rtcEngineConfig.mContext = baseContext
            rtcEngineConfig.mAppId = appId
            rtcEngineConfig.mEventHandler = mRtcEventHandler
            agoraEngine = RtcEngine.create(rtcEngineConfig)
            agoraEngine!!.enableVideo()
        }catch (e: Exception){
            showMessage(e.toString())
        }
    }

    private val mRtcEventHandler : IRtcEngineEventHandler = object  : IRtcEngineEventHandler() {

        override fun onUserJoined(uid: Int, elapsed: Int) {
            showMessage("Remote user joined $uid")

            runOnUiThread {
                setupRemoteVideo(uid)
            }
        }

        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            isJoined = true
            showMessage("Joined channel $channel")

        }

        override fun onUserOffline(uid: Int, reason: Int) {
            showMessage("Remote user offline $uid $reason")
            runOnUiThread {
                remoteView!!.visibility = View.GONE
            }
        }

    }

    private fun setupRemoteVideo(uId: Int){
        val container = binding.remoteVideoViewContainer
        remoteView = SurfaceView(baseContext)
        remoteView!!.setZOrderMediaOverlay(true)
        container.addView(remoteView)
        agoraEngine?.setupRemoteVideo(VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_FIT, uId))
        remoteView!!.visibility = View.VISIBLE

    }

    private fun setupLocalVideo(){
        val container = binding.localVideoViewContainer
        localView = SurfaceView(baseContext)
        localView!!.setZOrderMediaOverlay(true)
        container.addView(localView)
        agoraEngine?.setupLocalVideo(VideoCanvas(localView, VideoCanvas.RENDER_MODE_FIT, 0))
        localView!!.visibility = View.VISIBLE

    }

    private fun leaveCall() {
        if (!isJoined){
            showMessage("join a channel first")
        }else{
            agoraEngine!!.leaveChannel()
            showMessage("you left the channel")
            if (remoteView != null) remoteView!!.visibility = View.GONE
            if (localView != null) localView!!.visibility = View.GONE
            isJoined = false
        }
    }

    private fun joinCall() {
        if (checkPermission()){
            val option = ChannelMediaOptions()
            option.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
            option.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            setupLocalVideo()
            localView!!.visibility = View.VISIBLE
            agoraEngine!!.startPreview()
            agoraEngine!!.joinChannel(token, channelName, uId, option)
        }else{
            Toast.makeText(this, "Permission not granted", Toast.LENGTH_LONG).show()
        }
    }

    private fun showMessage(message: String){
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }
    }


    private fun checkPermission () :Boolean {
        if (ContextCompat.checkSelfPermission(this, listOfPermission[0]) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, listOfPermission[1]) != PackageManager.PERMISSION_GRANTED){

            return false
        }

        return true
    }




}