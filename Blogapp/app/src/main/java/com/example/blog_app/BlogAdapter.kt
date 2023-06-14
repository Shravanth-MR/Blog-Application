package com.example.blog_app

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class BlogAdapter : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_blog_adapter)
//    }
//}
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.blog_app.R
import com.squareup.picasso.Picasso

class BlogAdapter : ListAdapter<Blog, BlogAdapter.BlogViewHolder>(BlogDiffCallback()) {

    private var onItemClickListener: ((Blog) -> Unit)? = null

    fun setOnItemClickListener(listener: (Blog) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
        return BlogViewHolder(view)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val currentBlog = getItem(position)
        holder.bind(currentBlog)
    }

    inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
        private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
        private val dateTextView: TextView = itemView.findViewById(R.id.dateTextView)
        private val blogImageView: ImageView = itemView.findViewById(R.id.blogImageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)


        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val blog = getItem(position)
                    onItemClickListener?.invoke(blog)
                }
            }
        }

        fun bind(blog: Blog) {
            // Bind the data to the views
            itemView.apply {
                usernameTextView.text = blog.username
                dateTextView.text = blog.timestamp.toString()
                titleTextView.text = blog.title
                descriptionTextView.text = blog.description

                // Load the profile image
                val profileImagePlaceholder = R.drawable.default_profile_image
                val profileImageView = findViewById<ImageView>(R.id.profileImageView) // Replace 'profileImageView' with the actual ID of the profile image view

                if (blog.profileImageUrl.isNullOrEmpty()) {
                    profileImageView.setImageResource(profileImagePlaceholder)
                } else {
                    Picasso.get()
                        .load(blog.profileImageUrl)
                        .placeholder(profileImagePlaceholder)
                        .into(profileImageView)
                }

                // Load the blog image if available
                val blogImageView = findViewById<ImageView>(R.id.blogImageView) // Replace 'blogImageView' with the actual ID of the blog image view

                if (blog.imageUrl.isNullOrEmpty()) {
                    blogImageView.visibility = View.GONE
                } else {
                    Picasso.get()
                        .load(blog.imageUrl)
                        .into(blogImageView)
                    blogImageView.visibility = View.VISIBLE
                }
            }
        }






    }


    class BlogDiffCallback : DiffUtil.ItemCallback<Blog>() {
        override fun areItemsTheSame(oldItem: Blog, newItem: Blog): Boolean {
            return oldItem.blogId == newItem.blogId
        }

        override fun areContentsTheSame(oldItem: Blog, newItem: Blog): Boolean {
            return oldItem == newItem
        }
    }
}
