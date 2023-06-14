package com.example.blog_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase


class HomepageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button

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
        auth = Firebase.auth
        logoutButton = findViewById(R.id.logout_button)

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
        }
        fetchData()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun fetchData() {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")
        blogsRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val blogList = ArrayList<Blog>()
                for (document in querySnapshot) {
                    val blog = document.toObject(Blog::class.java)
                    blogList.add(blog)
                }
                blogAdapter.submitList(blogList)
            }
            .addOnFailureListener { exception ->
                showToast("Failed to fetch blog data: ${exception.message}")
            }
        blogAdapter.setOnItemClickListener { blog ->
            showToast("Clicked on blog: ${blog.title}")
        }
    }
}