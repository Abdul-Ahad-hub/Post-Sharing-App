package com.example.postsharingapp

import FirestorePost
import ImgBBApiDirect
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.postsharingapp.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EditProfile : AppCompatActivity() {

    private lateinit var profileImage: ImageView
    private lateinit var nicknameField: EditText
    private lateinit var updateProfileBtn: Button
    private var selectedImageUri: Uri? = null
    private lateinit var api: ImgBBApiDirect
    private lateinit var currentImageURL:String
    private lateinit var progressBar: ProgressBar

    private val apiKey = "afec957ae83fabc35a2274d7b72a17f9"

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            profileImage.setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        profileImage = findViewById(R.id.profileImage)
        nicknameField = findViewById(R.id.nicknameField)
        updateProfileBtn = findViewById(R.id.updateProfileBtn)
        progressBar = findViewById(R.id.progress_upload)

        profileImage.setOnClickListener {
         imagePickerLauncher.launch("image/*")
        }

        val user = FirebaseAuth.getInstance().currentUser
       nicknameField.setText(user?.displayName)
        Glide.with(this).load(user?.photoUrl).circleCrop().into(profileImage)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ImgBBApiDirect::class.java)

        updateProfileBtn.setOnClickListener {
            val username = nicknameField.text.toString().trim()
            progressBar.visibility = View.VISIBLE
            if (username.isEmpty()) {
                showToast("Please enter a nickname")
                return@setOnClickListener
            }

            if (selectedImageUri != null) {
                uploadImageToImgBB(selectedImageUri!!) { imageUrl ->
                    updateProfile(username, imageUrl)
                }
            } else {

                updateProfile(username, "")
            }

        }
    }

    fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
    private fun uploadImageToImgBB(imageUri: Uri, callback: (String) -> Unit) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            api.uploadImage(apiKey, base64Image).enqueue(object : Callback<ImgbbResponse> {
                override fun onResponse(
                    call: Call<ImgbbResponse>,
                    response: Response<ImgbbResponse>
                ) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.data?.url
                        if (imageUrl != null) {
                            callback(imageUrl)
                        } else {
                            showToast("Failed to get image URL")
                        }
                    } else {
                        showToast("Upload failed: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<ImgbbResponse>, t: Throwable) {
                    showToast("Error: ${t.message}")
                }
            })

        } catch (e: Exception) {
            showToast("Error reading image: ${e.message}")
        }
    }

    private fun updateProfile (username:String, imageUrl:String){
       val user = FirebaseAuth.getInstance().currentUser;
        if(user != null){
            val builder = UserProfileChangeRequest.Builder()
                .setDisplayName(username)

            if (!imageUrl.isNullOrBlank()) {
                builder.setPhotoUri(Uri.parse(imageUrl))
            }

            val profileUpdates = builder.build()

            user.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        PostRepository.loadPostsFromFirestore(
                            this,
                            onlyMine = true
                        ) { loadedPosts ->
                            val db = FirebaseFirestore.getInstance()
                            for (post in loadedPosts) {
                                db.collection("posts")
                                    .document(post.id)
                                    .update(
                                        mapOf(
                                            "posterName" to username,
                                            "profileUrl" to imageUrl
                                        )
                                    )
                            }
                        }
                        progressBar.visibility = View.GONE
                        startActivity(Intent(this, MainActivity::class.java))
                        showToast("Profile Updated Successfully")
                    }
                }
                .addOnFailureListener {
                    showToast("Profile update failed: ${it.message}")
                }

        }


    }



}



