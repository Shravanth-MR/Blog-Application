//package com.example.blog_app
//
//
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import com.bumptech.glide.Glide
//import com.google.firebase.firestore.FirebaseFirestore
////import kotlinx.android.synthetic.main.activity_update_blog.*
//
//class UpdateBlogActivity : AppCompatActivity() {
//    private lateinit var blogId: String
//    private lateinit var blog: Blog
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_update_blog)
//
//        // Get the blogId passed from the previous activity
//        blogId = intent.getStringExtra("blogId") ?: ""
//
//        // Load the blog details for editing
//        loadBlogDetails()
//
//        // Handle update button click
//        updateButton1.setOnClickListener {
//            updateBlog()
//        }
//    }
//
//    private fun loadBlogDetails() {
//        // Retrieve the blog document based on the blogId
//        val db = FirebaseFirestore.getInstance()
//        val blogRef = db.collection("blogs").document(blogId)
//
//        blogRef.get()
//            .addOnSuccessListener { documentSnapshot ->
//                if (documentSnapshot.exists()) {
//                    blog = documentSnapshot.toObject(Blog::class.java)
//                    blog?.let {
//                        // Set the existing blog details in the UI for editing
//                        editTextTitle.setText(it.title)
//                        editTextContent.setText(it.description)
//                        // Load and display the blog image using Glide or Picasso
//                        if (!it.image.isNullOrEmpty()) {
//                            Glide.with(this)
//                                .load(it.image)
//                                .placeholder(R.drawable.placeholder_image)
//                                .into(imageView)
//                        }
//                        // Set other fields if available
//                    }
//                } else {
//                    // Blog document not found
//                    showToast("Blog not found", Toast.LENGTH_SHORT)
//                    finish()
//                }
//            }
//            .addOnFailureListener { error ->
//                showToast("Failed to retrieve blog details: ${error.message}", Toast.LENGTH_SHORT)
//                finish()
//            }
//    }
//
//    private fun updateBlog() {
//        val title = editTextTitle.text.toString().trim()
//        val description = editTextContent.text.toString().trim()
//        val imageUrl = blog?.image // Retrieve the existing image URL or update it with the new image URL
//
//        // Perform validation on the input fields if required
//
//        // Update the blog document in Firestore
//        val db = FirebaseFirestore.getInstance()
//        val blogRef = db.collection("blogs").document(blogId)
//
//        blogRef.update(
//            "title", title,
//            "description", description,
//            "image", imageUrl // Update the image URL field
//            // Update other fields if required
//        )
//            .addOnSuccessListener {
//                showToast("Blog updated successfully", Toast.LENGTH_SHORT)
//                finish()
//            }
//            .addOnFailureListener { error ->
//                showToast("Failed to update blog: ${error.message}", Toast.LENGTH_SHORT)
//            }
//    }
//
//    private fun showToast(message: String, duration: Int) {
//        Toast.makeText(this, message, duration).show()
//    }
//}
