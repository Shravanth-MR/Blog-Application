package com.example.blog_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class Blogpost : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private var selectedImageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blogpost)

        val topBar = findViewById<RelativeLayout>(R.id.topBar)
        val leftArrowButton = findViewById<ImageButton>(R.id.leftArrowButton)
        val postButton = findViewById<Button>(R.id.postButton)
        val imageButton = findViewById<Button>(R.id.imageButton)
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)

        imageView = findViewById(R.id.imageView)

        leftArrowButton.setOnClickListener {
            finish()
        }

        imageButton.setOnClickListener {
            val imagePicker = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(imagePicker, REQUEST_IMAGE_PICK)
        }

        postButton.setOnClickListener {
            val title = titleEditText.text.toString()
            val description = descriptionEditText.text.toString()

            if (title.isNotEmpty() && description.isNotEmpty()) {
                val userId = FirebaseAuth.getInstance().currentUser?.uid
                val timestamp = FieldValue.serverTimestamp()

                if (userId != null) {
                    val db = FirebaseFirestore.getInstance()
                    val blogsRef = db.collection("blogs")

                    val blogData = hashMapOf(
                        "userId" to userId,
                        "title" to title,
                        "description" to description,
                        "timestamp" to timestamp
                    )

                    blogsRef.add(blogData)
                        .addOnSuccessListener { documentReference ->
                            val blogId = documentReference.id

                            if (selectedImageUri != null) {
                                val storageRef = Firebase.storage.reference
                                val imageRef = storageRef.child("images/$blogId.jpg")

                                imageRef.putFile(selectedImageUri!!)
                                    .addOnSuccessListener {
                                        imageRef.downloadUrl.addOnSuccessListener { imageUrl ->
                                            val updatedBlogData = hashMapOf(
                                                "image" to imageUrl.toString()
                                            )
                                            blogsRef.document(blogId).update("image", imageUrl.toString())
                                                .addOnSuccessListener {
                                                    showToast("Blog posted successfully!")
                                                    finish()
                                                }
                                                .addOnFailureListener { error ->
                                                    showToast("Failed to update blog entry: ${error.message}")
                                                }

                                        }
                                    }
                                    .addOnFailureListener { error ->
                                        showToast("Failed to upload image: ${error.message}")
                                    }
                            } else {
                                showToast("Blog posted successfully!")
                                finish()
                            }
                        }
                        .addOnFailureListener { error ->
                            showToast("Failed to create blog entry: ${error.message}")
                        }
                } else {
                    showToast("User not authenticated")
                }
            } else {
                showToast("Please enter a title and description")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data

            imageView.setImageURI(selectedImageUri)

            // Adjust the size of the image view
            imageView.layoutParams.width = 500
            imageView.layoutParams.height = 500
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val REQUEST_IMAGE_PICK = 1
    }
}
