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
import java.text.SimpleDateFormat
import java.util.Locale


class BlogAdapterSearch : ListAdapter<Blog, BlogAdapterSearch.BlogViewHolder>(BlogDiffCallback()) {
    private var searchQuery: String? = null

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
//            timestampTextView.text = blog.timestamp.toString()
            titleTextView.text = blog.title

            val dateFormat = SimpleDateFormat("EEE, MMM dd yyyy, hh:mm a", Locale.getDefault())
            val formattedDate = dateFormat.format(blog.timestamp)
            timestampTextView.text = formattedDate

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

            if (!searchQuery.isNullOrEmpty()) {
                highlightSearchText(blog)
            } else {
                descriptionTextView.text = blog.description
            }

        }

        private fun highlightSearchText(blog: Blog) {
            val searchText = searchQuery?.toLowerCase()
            val description = blog.description?.toLowerCase()
            if (searchText != null && description != null) {
                val startIndex = description.indexOf(searchText)
                if (startIndex != -1) {
                    val endIndex = startIndex + searchText.length
                    val highlightedText = StringBuilder()
                    highlightedText.append(description.substring(0, startIndex))
                    highlightedText.append("<font color='#FF0000'>")
                    highlightedText.append(description.substring(startIndex, endIndex))
                    highlightedText.append("</font>")
                    highlightedText.append(description.substring(endIndex))
                    descriptionTextView.text =
                        android.text.Html.fromHtml(
                            highlightedText.toString(),
                            android.text.Html.FROM_HTML_MODE_LEGACY
                        )

                } else {
                    descriptionTextView.text = blog.description
                }
            }
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


        fun searchBlogs(query: String) {
            searchQuery = query
            notifyDataSetChanged()
        }
}
