package com.example.blog_app

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.blog_app.Blog
import com.example.blog_app.BlogAdapterSearch
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.Locale


class SearchActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BlogAdapterSearch

    private lateinit var editTextSearch: EditText
    private lateinit var buttonSearch: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        recyclerView = findViewById(R.id.recyclerView)
        adapter = BlogAdapterSearch()

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        editTextSearch = findViewById(R.id.editTextSearch)
        buttonSearch = findViewById(R.id.buttonSearch)

        buttonSearch.setOnClickListener {
            val searchQuery = editTextSearch.text.toString().trim()
            if (searchQuery.isNotEmpty()) {
                performSearch(searchQuery)
            }
        }
    }


    private fun performSearch(query: String) {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")

        blogsRef.orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val blogList = ArrayList<Blog>()
                val userIds = HashSet<String>() // Store unique user IDs

                // Collect user IDs from blogs that need profile data
                for (document in querySnapshot) {
                    val blog = document.toObject(Blog::class.java)
                    val userId = blog.userId
                    if (!userId.isNullOrBlank()) {
                        userIds.add(userId)
                    }

                    // Convert the title and description to lowercase for case-insensitive comparison
                    val lowercaseTitle = blog.title?.lowercase(Locale.ROOT)
                    val lowercaseDescription = blog.description?.lowercase(Locale.ROOT)

                    // Check if the query matches the lowercase title or description
                    if (lowercaseTitle?.contains(query.lowercase(Locale.ROOT)) == true ||
                        lowercaseDescription?.contains(query.lowercase(Locale.ROOT)) == true) {
                        blogList.add(blog)
                    }
                }

                // Fetch user profiles in a batched request
                val userProfilePromises = ArrayList<Task<DocumentSnapshot>>()
                for (userId in userIds) {
                    val userProfileRef = db.collection("userProfile").document(userId)
                    val userProfilePromise = userProfileRef.get()
                    userProfilePromises.add(userProfilePromise)
                }

                Tasks.whenAllSuccess<DocumentSnapshot>(userProfilePromises)
                    .addOnSuccessListener { userProfileSnapshots ->
                        // Process user profile data
                        for (userProfileSnapshot in userProfileSnapshots) {
                            if (userProfileSnapshot.exists()) {
                                val userId = userProfileSnapshot.id
                                val username = userProfileSnapshot.getString("username")
                                val profileImageUrl = userProfileSnapshot.getString("image_url")

                                // Update corresponding blogs with user profile data
                                for (blog in blogList) {
                                    if (blog.userId == userId) {
                                        blog.username = username
                                        blog.profileImageUrl = profileImageUrl
                                    }
                                }
                            }
                        }

                        // Update the adapter with the modified blog list
                        adapter.submitList(blogList)
                    }
                    .addOnFailureListener { error ->
                        showToast("Failed to fetch user profiles: ${error.message}")
                    }
            }
            .addOnFailureListener { error ->
                showToast("Failed to perform search: ${error.message}")
            }
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}