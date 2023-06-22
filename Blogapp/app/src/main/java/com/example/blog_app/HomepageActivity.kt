package com.example.blog_app

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
//import kotlin.coroutines.jvm.internal.CompletedContinuation.context


class HomepageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var blogAdapter: BlogAdapter
    private lateinit var auth: FirebaseAuth
//    private lateinit var logoutButton: Button
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
//        auth = Firebase.auth
//        logoutButton = findViewById(R.id.logout_button)

        val profileButton = findViewById<Button>(R.id.profileButton)

        profileButton.setOnClickListener {
            val intent = Intent(this, Profpage::class.java)
            startActivity(intent)
        }
        searchButton = findViewById(R.id.searchButton)
        searchButton.setOnClickListener {
            val intent = Intent(this, SearchActivity::class.java)
            startActivity(intent)
        }

//        logoutButton.setOnClickListener {
//            auth.signOut()
//            val intent = Intent(this, Login::class.java)
//            startActivity(intent)
//            finish()
//        }
        fetchData()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun fetchData() {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")
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
                        db.collection("userProfile").document(blog.userId!!).get()
                            .addOnCompleteListener { userTask ->
                                if (userTask.isSuccessful) {
                                    val userDocument = userTask.result
                                    if (userDocument != null && userDocument.exists()) {
                                        blog.username = userDocument.getString("username")
                                        blog.profileImageUrl = userDocument.getString("image_url")
                                        blogList.add(blog)
                                    } else {
                                        showToast("User document not found for ${blog.userId}")
                                    }
                                } else {
                                    showToast("Failed to fetch user data: ${userTask.exception?.message}")
                                }

                                // Move the submitList() outside the loop
                                blogAdapter.submitList(blogList)
                            }
                    }
                }
            }

        // No need for addOnCompleteListener here

        blogAdapter.setOnItemClickListener { blog ->
            showToast("Created by: ${blog.username}")
        }
    }

}

