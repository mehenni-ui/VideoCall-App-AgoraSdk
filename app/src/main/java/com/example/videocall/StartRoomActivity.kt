package com.example.videocall

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.videocall.databinding.ActivityStartRoomBinding

class StartRoomActivity : AppCompatActivity() {
    private lateinit var binding: ActivityStartRoomBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStartRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnJoinId.setOnClickListener {
            val roomName = binding.roomNameId.text.toString()
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("channelName", roomName)
            startActivity(intent)
        }
    }
}