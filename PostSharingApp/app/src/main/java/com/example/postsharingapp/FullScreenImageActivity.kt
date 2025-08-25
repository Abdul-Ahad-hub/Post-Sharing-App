package com.example.postsharingapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.jsibbold.zoomage.ZoomageView


class FullScreenImageActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_full_screen_image)

        val photoView = findViewById<ZoomageView>(R.id.fullscreenImageView)
        val imageUrl = intent.getStringExtra("imageUrl")


        Glide.with(this)
            .load(imageUrl)
            .into(photoView)
    }
}
