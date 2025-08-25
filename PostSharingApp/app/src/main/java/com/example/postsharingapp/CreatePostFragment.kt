package com.example.postsharingapp

import FirestorePost
import ImgBBApiDirect
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.example.postsharingapp.BuildConfig
import com.google.firebase.firestore.FirebaseFirestore
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreatePostFragment : Fragment() {

    private lateinit var postImage: ImageView
    private lateinit var chooseImgBtn: Button
    private lateinit var submitBtn: Button
    private lateinit var editTextContent: EditText
    private lateinit var progressBar: ProgressBar
    private var selectedImageUri: Uri? = null

    private val apiKey = BuildConfig.FIREBASE_API_KEY;
    private lateinit var api: ImgBBApiDirect

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            selectedImageUri = it
            postImage.setImageURI(it)
            postImage.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.imgbb.com/1/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        api = retrofit.create(ImgBBApiDirect::class.java)

        postImage = view.findViewById(R.id.image_preview)
        chooseImgBtn = view.findViewById(R.id.btn_choose_image)
        submitBtn = view.findViewById(R.id.btn_post)
        editTextContent = view.findViewById(R.id.edit_caption)
        progressBar = view.findViewById(R.id.progress_upload)

        chooseImgBtn.setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }

        submitBtn.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            submitBtn.isEnabled = false
            chooseImgBtn.isEnabled = false

            val content = editTextContent.text.toString().trim()



            if (content.isEmpty()) {
                showToast("Please enter a caption")
                resetUI()
                return@setOnClickListener
            }

            val user = FirebaseAuth.getInstance().currentUser
            val userName = user?.displayName ?: "Anonymous"
            val userProfilePicLink = user?.photoUrl
            val userId = user?.uid.toString()

            selectedImageUri?.let { uri ->
                uploadImageToImgBB(uri) { imageUrl ->
                    uploadImageToImgBB(selectedImageUri!!) { imageUrl ->
                        savePost(
                            userId,
                            userName,
                            content,
                            imageUrl,
                            userProfilePicLink.toString(),
                            onSuccess = {
                                resetUI()
                                showToast("Post saved successfully!")
                                startActivity(Intent(requireContext(), MainActivity::class.java))
                            },
                            onError = { e ->
                                showToast("Failed to save post: ${e.message}")
                                resetUI()
                            }
                        )
                    }
                }
            } ?: run {
                savePost(
                    userId,
                    userName,
                    content,
                    " ",
                    userProfilePicLink.toString(),
                    onSuccess = {
                        resetUI()
                        showToast("Post saved successfully!")
                        startActivity(Intent(requireContext(), MainActivity::class.java))
                    },
                    onError = { e ->
                        showToast("Failed to save post: ${e.message}")
                        resetUI()
                    })
            }


        }
    }

    private fun savePost(
        userId:String,
        userName: String,
        content: String,
        imageUrl: String,
        profileUrl: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        val date = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        val post = FirestorePost(userId,userName, content, date, imageUrl, profileUrl)

        db.collection("posts")
            .add(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    private fun uploadImageToImgBB(imageUri: Uri, callback: (String) -> Unit) {
        try {
            val inputStream: InputStream? = requireContext().contentResolver.openInputStream(imageUri)
            val bytes = inputStream?.readBytes()
            if (bytes == null || bytes.isEmpty()) {
                showToast("Error reading image")
                resetUI()
                return
            }
            val base64Image = Base64.encodeToString(bytes, Base64.DEFAULT)

            api.uploadImage(apiKey, base64Image).enqueue(object : Callback<ImgbbResponse> {
                override fun onResponse(call: Call<ImgbbResponse>, response: Response<ImgbbResponse>) {
                    if (response.isSuccessful) {
                        val imageUrl = response.body()?.data?.url
                        if (imageUrl != null) {
                            callback(imageUrl)
                        } else {
                            showToast("Failed to get image URL")
                            resetUI()
                        }
                    } else {
                        showToast("Upload failed: ${response.message()}")
                        resetUI()
                    }
                }

                override fun onFailure(call: Call<ImgbbResponse>, t: Throwable) {
                    showToast("Error: ${t.message}")
                    resetUI()
                }
            })

        } catch (e: Exception) {
            showToast("Error reading image: ${e.message}")
            resetUI()
        }
    }

    private fun resetUI() {
        progressBar.visibility = View.GONE
        submitBtn.isEnabled = true
        chooseImgBtn.isEnabled = true
    }

    private fun showToast(msg: String) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
    }
}
