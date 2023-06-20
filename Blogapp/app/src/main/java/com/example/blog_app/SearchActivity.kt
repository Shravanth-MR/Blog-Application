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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.util.regex.Pattern


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


    private fun performSearch(searchQuery: String) {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")

        val regexQuery = searchQuery.trim().toLowerCase().replace("\\s+".toRegex(), ".*")

        blogsRef.whereMatchesCaseInsensitive("title", regexQuery)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val blogs = mutableListOf<Blog>()
                for (document in querySnapshot) {
                    val blog = document.toObject(Blog::class.java)
                    blogs.add(blog)
                }
                adapter.submitList(blogs)
            }
            .addOnFailureListener { error ->
                // Handle the error gracefully
                Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
    }

    fun Query.whereMatchesCaseInsensitive(field: String, regex: String): Query {
        return whereGreaterThanOrEqualTo(field, regex.toLowerCase())
            .whereLessThanOrEqualTo(field, regex.toLowerCase() + "\uf8ff")
    }

    fun Query.whereMatches(field: String, regex: String): Query {
        return whereGreaterThanOrEqualTo(field, regex)
            .whereLessThanOrEqualTo(field, regex + "\uf8ff")
    }

    fun Query.whereRegex(field: String, regex: String): Query {
        return whereGreaterThanOrEqualTo(field, regex)
            .whereLessThanOrEqualTo(field, regex + "\uf8ff")
    }

}
