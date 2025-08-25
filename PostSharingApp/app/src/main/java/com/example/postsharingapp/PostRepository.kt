package com.example.postsharingapp.repository

import FirestorePost
import android.content.Context
import android.widget.Toast
import com.example.postsharingapp.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PostRepository {

    fun loadPostsFromFirestore(
        context: Context,
        onlyMine: Boolean = false,
        onPostsLoaded: (List<Post>) -> Unit
    ) {
        val db = FirebaseFirestore.getInstance()
        var query: Query = db.collection("posts")

        if (onlyMine) {
            val user = FirebaseAuth.getInstance().currentUser
            val id = user?.uid.toString()
            query = query.whereEqualTo("userId", id)
        }

        query.get()
            .addOnSuccessListener { result ->
                val postList = mutableListOf<Post>()
                for (doc in result) {
                    val firePost = doc.toObject(FirestorePost::class.java)

                    val formattedDate = try {
                        parseDate(firePost.postDate)
                    } catch (e: Exception) {
                        firePost.postDate
                    }

                    postList.add(
                        Post(
                            id = doc.id,
                            userId = firePost.userId,
                            content = firePost.content,
                            posterName = firePost.posterName,
                            postDate = formattedDate,
                            imageUrl = firePost.photoUrl,
                            profileUrl = firePost.profileUrl
                        )
                    )
                }
                onPostsLoaded(postList)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to load posts", Toast.LENGTH_SHORT).show()
            }
    }

    private fun parseDate(dateString: String): String {
        val parser = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        val date = parser.parse(dateString) ?: Date()

        val diffMillis = Date().time - date.time
        val minutes = diffMillis / (1000 * 60)
        val hours = diffMillis / (1000 * 60 * 60)
        val days = diffMillis / (1000 * 60 * 60 * 24)

        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "$minutes minute${if (minutes > 1) "s" else ""} ago"
            hours < 24 -> "$hours hour${if (hours > 1) "s" else ""} ago"
            days == 1L -> "yesterday"
            days < 7 -> "$days day${if (days > 1) "s" else ""} ago"
            else -> SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(date)
        }
    }
}
