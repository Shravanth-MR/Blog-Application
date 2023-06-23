package com.example.blog_app

import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class Profile : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var btnBrowse: Button
    private lateinit var btnUpload: Button
    private lateinit var usernameEditText: EditText
    private lateinit var titleEditText: EditText

    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null

    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        storageRef = Firebase.storage.reference

        image = findViewById(R.id.profileImageView)
        btnBrowse = findViewById(R.id.selectImageButton)
        btnUpload = findViewById(R.id.saveButton)
        usernameEditText = findViewById(R.id.usernameEditText)
        titleEditText = findViewById(R.id.titleEditText)

        galleryImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val selectedImageUri: Uri? = data.data
                    if (selectedImageUri != null) {
                        image.setImageURI(selectedImageUri)
                        uri = selectedImageUri
                    }
                }
            }
        }

        btnBrowse.setOnClickListener {
            openGallery()
        }

        btnUpload.setOnClickListener {
            uploadImage()
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        galleryImageLauncher.launch(intent)
    }

    private fun uploadImage() {
        val username = usernameEditText.text.toString().trim()
        val title = titleEditText.text.toString().trim()

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val db = FirebaseFirestore.getInstance()
            val userImagesRef = db.collection("userProfile")
            val user = hashMapOf(
                "username" to username,
                "title" to title
            )

            if (uri != null) {
                val imageRef = storageRef.child("images").child(System.currentTimeMillis().toString())

                imageRef.putFile(uri!!)
                    .addOnSuccessListener { uploadTask ->
                        uploadTask.storage.downloadUrl
                            .addOnSuccessListener { downloadUri ->
                                val imageUrl = downloadUri.toString()
                                user["image_url"] = imageUrl

                                userImagesRef.document(userId).set(user)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                                        navigateToHome()
                                    }
                                    .addOnFailureListener { error ->
                                        Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                            .addOnFailureListener { error ->
                                Toast.makeText(this, "Error retrieving download URL: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // If uri is null, save the user data without the image_url
                userImagesRef.document(userId).set(user)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Upload successful", Toast.LENGTH_SHORT).show()
                        navigateToHome()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, HomepageActivity::class.java)
        startActivity(intent)
        finish()
    }
}
