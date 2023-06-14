package com.example.blog_app

//import android.os.Bundle
//import androidx.appcompat.app.AppCompatActivity
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.blog_app.R
//import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.firestore.Query

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blog_app.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore

class HomepageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = BlogAdapter()
        recyclerView.adapter = blogAdapter

        // Fetch blog data from Firestore
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")

        blogsRef.get()
            .addOnSuccessListener { querySnapshot ->
                val blogList = ArrayList<Blog>()
                for (document in querySnapshot) {
                    val blog = document.toObject(Blog::class.java)
                    blogList.add(blog)
                }
                blogAdapter.submitList(blogList)
            }
            .addOnFailureListener { exception ->
                // Handle any errors that occurred while fetching data
                showToast("Failed to fetch blog data: ${exception.message}")
            }

        // Handle click on a blog item
        blogAdapter.setOnItemClickListener { blog ->
            // Open the blog detail activity or perform any desired action
            // You can access the blog object and its properties here
            showToast("Clicked on blog: ${blog.title}")
        }

        // Add a new blog button click listener
        val addBlogButton = findViewById<FloatingActionButton>(R.id.addBlogButton)
        addBlogButton.setOnClickListener {
            // Open the activity to add a new blog
            val intent = Intent(this, Blogpost::class.java)
            startActivity(intent)
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
