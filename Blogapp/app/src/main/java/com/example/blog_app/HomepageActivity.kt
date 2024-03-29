package com.example.blog_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class HomepageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var searchButton: FloatingActionButton

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        blogAdapter = BlogAdapter()
        recyclerView.adapter = blogAdapter

        val addBlogButton = findViewById<FloatingActionButton>(R.id.addBlogButton)
        addBlogButton.setOnClickListener {
            val intent = Intent(this, Blogpost::class.java)
            startActivity(intent)
        }

        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        profileButton.setOnClickListener {
            val intent = Intent(this, Profpage::class.java)
            startActivity(intent)
        }
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

        fetchData()
    }
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    private fun fetchData() {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")
        val userProfileRef = db.collection("userProfile")
        // Listen for real-time updates on the blogs collection
        blogsRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { querySnapshot, error ->
                if (error != null) {
                    showToast("Failed to fetch blog data: ${error.message}")
                    return@addSnapshotListener
                }
                if (querySnapshot != null) {
                    val blogList = ArrayList<Blog>()
                    for (document in querySnapshot) {
                        val blog = document.toObject(Blog::class.java)
                        blogList.add(blog)
                        // Retrieve the user profile in real-time
                        userProfileRef.document(blog.userId!!).addSnapshotListener { userDocument, userError ->
                            if (userError != null) {
                                showToast("Failed to fetch user data: ${userError.message}")
                                return@addSnapshotListener
                            }

                            if (userDocument != null && userDocument.exists()) {
                                blog.username = userDocument.getString("username")
                                blog.profileImageUrl = userDocument.getString("image_url")
                                blogAdapter.notifyDataSetChanged()
                            } else {
                                showToast("User document not found for ${blog.userId}")
                            }
                        }
                    }
                    blogAdapter.submitList(blogList)
                }
            }
        blogAdapter.setOnItemClickListener { blog ->
            showToast("Created by: ${blog.username}")
        }
    }

}
