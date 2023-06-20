    package com.example.blog_app


    import android.view.LayoutInflater
    import android.view.View
    import android.view.ViewGroup
    import android.widget.ImageView
    import android.widget.TextView
    import androidx.recyclerview.widget.DiffUtil
    import androidx.recyclerview.widget.ListAdapter
    import androidx.recyclerview.widget.RecyclerView
    import com.bumptech.glide.Glide
    import com.example.blog_app.Blog
    import com.example.blog_app.R
    import java.text.SimpleDateFormat
    import java.util.Locale

    class BlogAdapter : ListAdapter<Blog, BlogAdapter.BlogViewHolder>(BlogDiffCallback()) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_blog, parent, false)
            return BlogViewHolder(view)
        }

        private var onItemClickListener: ((Blog) -> Unit)? = null

        fun setOnItemClickListener(listener: (Blog) -> Unit) {
            onItemClickListener = listener
        }

        override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
            val blog = getItem(position)
            holder.bind(blog)
        }

        inner class BlogViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val profileImageView: ImageView = itemView.findViewById(R.id.profileImageView)
            private val usernameTextView: TextView = itemView.findViewById(R.id.usernameTextView)
            private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
            private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
            private val blogImageView: ImageView = itemView.findViewById(R.id.blogImageView)
            private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)

            fun bind(blog: Blog) {
                Glide.with(itemView.context)
                    .load(blog.profileImageUrl)
                    .circleCrop()
                    .placeholder(R.drawable.default_profile_image)
                    .into(profileImageView)

                usernameTextView.text = blog.username

                val timestamp = blog.timestamp?.let { SimpleDateFormat("MMM d, hh:mm a", Locale.getDefault()).format(it) }
                timestampTextView.text = timestamp

                titleTextView.text = blog.title


                if (blog.image != null) {
                    Glide.with(itemView.context)
                        .load(blog.image)
                        .placeholder(R.drawable.placeholder_image)
                        .into(blogImageView)
                } else {
                    blogImageView.setImageDrawable(null)
                }

                itemView.setOnClickListener {
                    onItemClickListener?.invoke(blog)
                }
                descriptionTextView.text = blog.description
            }
        }

        private class BlogDiffCallback : DiffUtil.ItemCallback<Blog>() {
            override fun areItemsTheSame(oldItem: Blog, newItem: Blog): Boolean {
                return oldItem.userId == newItem.userId
            }

            override fun areContentsTheSame(oldItem: Blog, newItem: Blog): Boolean {
                return oldItem == newItem
            }
        }
    }
