package com.example.blog_app

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
//import java.security.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale
import com.google.firebase.Timestamp


class Profpage : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var blogPostsLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profpage)

        profileImageView = findViewById(R.id.profileImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        titleTextView = findViewById(R.id.titleTextView)
        blogPostsLayout = findViewById(R.id.blogPostsLayout)
        val leftArrowButton = findViewById<ImageButton>(R.id.leftArrowButton)


        leftArrowButton.setOnClickListener {
            finish()
        }

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("userProfile").document(userId)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val username = documentSnapshot.getString("username")
                        val title = documentSnapshot.getString("title")
                        val imageUrl = documentSnapshot.getString("image_url")

                        usernameTextView.text = username
                        titleTextView.text = title

                        // Load profile image using Picasso or any other image loading library
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).into(profileImageView)
                        }
                    }
                }
                .addOnFailureListener { error ->
                // Handle the error gracefully
                    showToast("Failed to retrieve user profile: ${error.message}", Toast.LENGTH_SHORT)

                    Log.e("Profpage", "Failed to retrieve user profile", error)
            }

            val blogsRef = db.collection("blogs").whereEqualTo("userId", userId)

            blogsRef.get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot) {
                        val blogId = document.id
                        val blogData = document.data
                        val title = blogData["title"] as String
                        val description = blogData["description"] as String
                        val timestamp = blogData["timestamp"] as? Timestamp
                        val userId = blogData["userId"] as? String  // Fetch the userId from the blog data
                        val imageUrlblog = blogData["image_url"] as? String  // Fetch the image URL from the blog data

                        // Create a blog post view
                        val blogPostView = LayoutInflater.from(this).inflate(R.layout.item_blog_post, null)

                        // Set the profile image and username
                        val profileImageView = blogPostView.findViewById<ImageView>(R.id.profileImageView)
                        val usernameTextView = blogPostView.findViewById<TextView>(R.id.usernameTextView)

                        // Load the user profile image using Picasso or any other image loading library
                        val imageUrl = blogData["image_url"] as? String
                        if (!imageUrl.isNullOrEmpty()) {
                            Picasso.get().load(imageUrl).into(profileImageView)
                        }
                        val username = blogData["username"] as? String
                        usernameTextView.text = username

                        // Set the blog post details
                        val titleTextView = blogPostView.findViewById<TextView>(R.id.titleTextView)
                        val descriptionTextView = blogPostView.findViewById<TextView>(R.id.descriptionTextView)

                        titleTextView.text = title
                        descriptionTextView.text = description

                        // Set the blog post date
                        val dateTextView = blogPostView.findViewById<TextView>(R.id.dateTextView)
                        timestamp?.let {
                            val date = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(it.toDate())
                            dateTextView.text = date
                        }

                        // Set the background color of the profile image circle
                        val circleBackground = profileImageView.background as GradientDrawable
                        val color = ContextCompat.getColor(this, R.color.profile_image_circle_background)
                        circleBackground.setColor(color)



                        // Set the click listeners for edit and delete buttons
                        val editButton = blogPostView.findViewById<Button>(R.id.editButton)
                        val deleteButton = blogPostView.findViewById<Button>(R.id.deleteButton)

                        editButton.setOnClickListener {
                            // Handle edit button click
                            // Implement your logic here
                        }

                        deleteButton.setOnClickListener {
                            // Handle delete button click
                            // Implement your logic here
                        }

                        // Add the blog post view to the layout
                        blogPostsLayout.addView(blogPostView)
                    }
                }
                .addOnFailureListener { error ->
                    // Handle the error gracefully
                    showToast("Failed to retrieve blog posts: ${error.message}")
                    Log.e("Profpage", "Failed to retrieve blog posts", error)
                }
        }
    }

    private fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(this, message, duration).show()
    }

}