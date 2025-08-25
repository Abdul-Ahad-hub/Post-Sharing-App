package com.example.postsharingapp

data class Post(
    val id: String = "",
    val userId: String = "",
    val content: String = "",
    val posterName: String = "",
    val postDate: String = "",
    val imageUrl: String? = null,
    val profileUrl: String? = null
)
