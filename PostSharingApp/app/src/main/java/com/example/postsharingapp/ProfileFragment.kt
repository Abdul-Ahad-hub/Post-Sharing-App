package com.example.postsharingapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ItemTouchHelper
import com.bumptech.glide.Glide
import com.example.postsharingapp.repository.PostRepository
import com.example.postsharingapp.ui.login.LoginActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PostAdapter
    private val posts = mutableListOf<Post>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        recyclerView = view.findViewById(R.id.RecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = PostAdapter(posts)
        recyclerView.adapter = adapter

        view.findViewById<Button>(R.id.logoutbutton).setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        view.findViewById<MaterialButton>(R.id.btn_change_profile).setOnClickListener {
            val intent = Intent(context, EditProfile::class.java)
            startActivity(intent)
        }

        val user = FirebaseAuth.getInstance().currentUser
        view.findViewById<TextView>(R.id.user_name).text = FirebaseAuth.getInstance().currentUser?.displayName
        if (user?.photoUrl != null) {
            Glide.with(this).load(user.photoUrl).circleCrop().into(view.findViewById(R.id.profile_image))
        }

        PostRepository.loadPostsFromFirestore(
            requireContext(),
            onlyMine = true
        ) { loadedPosts ->
            posts.clear()
            posts.addAll(loadedPosts)
            adapter.notifyDataSetChanged()
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return
                val post = posts[position]
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                if (post.userId == currentUserId) {
                    FirebaseFirestore.getInstance()
                        .collection("posts")
                        .document(post.id)
                        .delete()
                        .addOnSuccessListener {
                            posts.removeAt(position)
                            Toast.makeText(requireContext(), "Post Successfully Deleted!", Toast.LENGTH_SHORT).show()
                            adapter.notifyItemRemoved(position)
                        }
                        .addOnFailureListener {
                            adapter.notifyItemChanged(position)
                        }
                } else {
                    adapter.notifyItemChanged(position)
                }
            }
        })
        itemTouchHelper.attachToRecyclerView(recyclerView)

        return view
    }
}
