package com.example.blog_app


import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Profpage : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var titleTextView: TextView
    private lateinit var blogPostsLayout: LinearLayout
    private lateinit var userId: String
    private lateinit var updateProfileButton: Button
    private lateinit var logoutButton: Button
    private lateinit var auth: FirebaseAuth
    private lateinit var userProfileListener: ListenerRegistration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profpage)

        profileImageView = findViewById(R.id.profileImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        titleTextView = findViewById(R.id.titleTextView)
        blogPostsLayout = findViewById(R.id.blogPostsLayout)
        logoutButton = findViewById(R.id.logoutButton)
        val leftArrowButton = findViewById<ImageButton>(R.id.leftArrowButton)

        leftArrowButton.setOnClickListener {
            finish()
        }

        updateProfileButton = findViewById(R.id.updateProfileButton)

        updateProfileButton.setOnClickListener {
            val username = usernameTextView.text.toString().trim()
            val title = titleTextView.text.toString().trim()

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("userProfile").document(userId)

            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val imageUrl = documentSnapshot.getString("image_url")

                        val intent = Intent(this, UpdateProfileActivity::class.java)
                        intent.putExtra("previousUsername", username)
                        intent.putExtra("previousTitle", title)
                        intent.putExtra("previousImageUrl", imageUrl)
                        startActivity(intent)
                    }
                }
        }

        auth = Firebase.auth
        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }

        userId = Firebase.auth.currentUser?.uid ?: ""
        if (userId.isNotEmpty()) {
            fetchUserBlogs()
        }

        retrieveUserProfile()
        listenForUserProfileUpdates()
    }

    private fun retrieveUserProfile() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("userProfile").document(userId)

            userProfileListener = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    showToast("Failed to retrieve user profile: ${error.message}", Toast.LENGTH_SHORT)
                    Log.e("Profpage", "Failed to retrieve user profile", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val username = snapshot.getString("username")
                    val title = snapshot.getString("title")
                    val imageUrl = snapshot.getString("image_url")

                    usernameTextView.text = username ?: ""
                    titleTextView.text = title ?: ""

                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImageView)
                    }
                }
            }
        }
    }

    private fun listenForUserProfileUpdates() {
        val userId = Firebase.auth.currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("userProfile").document(userId)

            userProfileListener = userRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Profpage", "Failed to listen for user profile updates", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val username = snapshot.getString("username")
                    val title = snapshot.getString("title")
                    val imageUrl = snapshot.getString("image_url")

                    usernameTextView.text = username ?: ""
                    titleTextView.text = title ?: ""

                    if (!imageUrl.isNullOrEmpty()) {
                        Picasso.get().load(imageUrl).into(profileImageView)
                    }
                }
            }
        }
    }

    private fun showToast(message: String, duration: Int) {
        Toast.makeText(this, message, duration).show()
    }

    private fun fetchUserBlogs() {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")

        blogsRef.whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val blog = document.toObject(Blog::class.java)
                    val timestamp = blog.timestamp
                    val blogItemView = LayoutInflater.from(this).inflate(R.layout.item_blog_post, blogPostsLayout, false)

                    // Set blog details
                    blogItemView.findViewById<TextView>(R.id.titleTextView).text = blog.title
                    blogItemView.findViewById<TextView>(R.id.contentTextView).text = blog.description
                    blogItemView.findViewById<TextView>(R.id.timestampTextView).text = formatDate(timestamp)

                    // Load and display the blog image using Glide
                    val imageView = blogItemView.findViewById<ImageView>(R.id.imageView1)
                    if (blog.image?.isNotEmpty() == true) {
                        Glide.with(this)
                            .load(blog.image)
                            .apply(RequestOptions().placeholder(R.drawable.placeholder_image)) // Placeholder image while loading
                            .into(imageView)
                    } else {
                        // No image available, so hide the ImageView
                        imageView.visibility = View.GONE
                    }

                    // Set click listeners for update and delete buttons
                    val updateButton = blogItemView.findViewById<Button>(R.id.updateButton)
                    val deleteButton = blogItemView.findViewById<Button>(R.id.deleteButton)

                    updateButton.setOnClickListener {
                        val blogId = document.id // Assuming document is the current blog document

                        // Start the activity to update the blog, passing the blogId as an extra
                        val intent = Intent(this@Profpage, UpdateBlogActivity::class.java)
                        intent.putExtra("blogId", blogId)

                        // Pass the previous title, description, and image URL to UpdateBlogActivity
                        intent.putExtra("previousTitle", blog.title)
                        intent.putExtra("previousDescription", blog.description)
                        intent.putExtra("previousImageUrl", blog.image)

                        startActivity(intent)
                    }

                    deleteButton.setOnClickListener {
                        val blogId = document.id // Assuming document is the current blog document

                        val db = FirebaseFirestore.getInstance()
                        val blogsRef = db.collection("blogs")

                        // Delete the blog from Firestore
                        blogsRef.document(blogId)
                            .delete()
                            .addOnSuccessListener {
                                // Blog deleted successfully, remove the blog item view from the layout
                                blogPostsLayout.removeView(blogItemView)
                            }
                            .addOnFailureListener { error ->
                                showToast("Failed to delete blog: ${error.message}", Toast.LENGTH_SHORT)
                                Log.e("Profpage", "Failed to delete blog", error)
                            }
                    }

                    // Add the blog item view to the blogPostsLayout
                    blogPostsLayout.addView(blogItemView)
                }
            }
            .addOnFailureListener { error ->
                showToast("Failed to retrieve user blogs: ${error.message}", Toast.LENGTH_SHORT)
                Log.e("Profpage", "Failed to retrieve user blogs", error)
            }
    }

    private fun formatDate(timestamp: Date?): String {
        return timestamp?.let {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.US)
            dateFormat.format(it)
        } ?: ""
    }

    override fun onDestroy() {
        super.onDestroy()
        userProfileListener.remove()
    }
}
