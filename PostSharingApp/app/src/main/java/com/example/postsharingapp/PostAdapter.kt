package com.example.postsharingapp

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView


class PostAdapter (val posts:List<Post>):RecyclerView.Adapter<PostAdapter.PostViewHolder>()
{
class PostViewHolder(val activeView:View):RecyclerView.ViewHolder(activeView){
    val postImage:ImageView = activeView.findViewById(R.id.imgPost)
    val caption:TextView = activeView.findViewById(R.id.tvCaption)
    val postDate: TextView = activeView.findViewById(R.id.tvDate)
    val poster: TextView = activeView.findViewById(R.id.tvName)
    val posterImg: ShapeableImageView = activeView.findViewById(R.id.imageProfile)


}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_post, parent, false)

        return PostViewHolder(view)
    }

    override fun getItemCount(): Int {
        return posts.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        if (post.imageUrl.isNullOrBlank()) {
            holder.postImage.visibility = View.GONE
        } else {
            holder.postImage.visibility = View.VISIBLE
            Glide.with(holder.itemView.context)
                .load(post.imageUrl)
                .into(holder.postImage)
        }

        holder.caption.text = post.content
        holder.postDate.text = post.postDate.toString()
        holder.poster.text = post.posterName
        Glide.with(holder.itemView.context)
            .load(post.profileUrl)
            .circleCrop()
            .into(holder.posterImg)


        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, DetailsView::class.java)
            intent.putExtra("posterName", post.posterName)
            intent.putExtra("postDate", post.postDate.toString())
            intent.putExtra("postText", post.content)
            intent.putExtra("profileImageUrl", post.profileUrl)
            intent.putExtra("postImageUrl", post.imageUrl)
            context.startActivity(intent)
    }
}
}