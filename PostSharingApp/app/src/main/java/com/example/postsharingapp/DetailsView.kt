package com.example.postsharingapp

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView

class DetailsView : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_view)
        val profileImage = findViewById<ShapeableImageView>(R.id.detailProfileImage)
        val posterName = findViewById<TextView>(R.id.detailPosterName)
        val postDate = findViewById<TextView>(R.id.detailPostDate)
        val postText = findViewById<TextView>(R.id.detailPostText)
        val postImage = findViewById<ImageView>(R.id.detailPostImage)

        val name = intent.getStringExtra("posterName")
        val date = intent.getStringExtra("postDate")
        val text = intent.getStringExtra("postText")
        val profileUrl = intent.getStringExtra("profileImageUrl")
        val postImageUrl = intent.getStringExtra("postImageUrl")

        posterName.text = name
        postDate.text = date
        postText.text = text

        Glide.with(this).load(profileUrl).circleCrop().into(profileImage)
        Glide.with(this).load(postImageUrl).into(postImage)

        postImage.setOnClickListener{

                val intent = Intent(this, FullScreenImageActivity::class.java)
                intent.putExtra("imageUrl", postImageUrl)
                startActivity(intent)

        }

        profileImage.setOnClickListener{

            val intent = Intent(this, FullScreenImageActivity::class.java)
            intent.putExtra("imageUrl", profileUrl)

            startActivity(intent)

        }




    }
}