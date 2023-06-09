package com.example.blog_app

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class Profpage : AppCompatActivity() {
    private lateinit var profileImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var titleTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profpage)

        profileImageView = findViewById(R.id.profileImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        titleTextView = findViewById(R.id.titleTextView)
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
                Toast.makeText(this, "Failed to retrieve user profile: ${error.message}", Toast.LENGTH_SHORT).show()
                Log.e("ProfileViewActivity", "Failed to retrieve user profile", error)
            }

        }
    }
}
