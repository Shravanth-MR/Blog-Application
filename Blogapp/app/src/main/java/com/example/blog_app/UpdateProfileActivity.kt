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
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

class UpdateProfileActivity : AppCompatActivity() {
    private lateinit var image: ImageView
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    private lateinit var usernameEditText: EditText
    private lateinit var titleEditText: EditText

    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null

    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        storageRef = Firebase.storage.reference

        image = findViewById(R.id.profileImageView)
        selectImageButton = findViewById(R.id.selectImageButton)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
        usernameEditText = findViewById(R.id.usernameEditText)
        titleEditText = findViewById(R.id.titleEditText)


        // Retrieve previous values from extras
        val previousUsername = intent.getStringExtra("previousUsername")
        val previousTitle = intent.getStringExtra("previousTitle")
        val previousImageUrl = intent.getStringExtra("previousImageUrl")

        // Set previous values to EditText fields
        usernameEditText.setText(previousUsername)
        titleEditText.setText(previousTitle)
        loadImageWithGlide(previousImageUrl, R.drawable.default_profile_image)


//        // Load previous image using previous image URL
//        Glide.with(this)
//            .load(previousImageUrl)
//            .into(image)

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


        selectImageButton.setOnClickListener {
            openGallery()
        }

        saveButton.setOnClickListener {
            updateProfile()
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadImageWithGlide(imageUrl: String?, placeholderImage: Int) {
        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .placeholder(placeholderImage)
                .error(placeholderImage)
                .into(image)
        } else {
            image.setImageResource(placeholderImage)
        }
    }


    private fun openGallery() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.type = "image/*"
        galleryImageLauncher.launch(intent)
    }

    private fun updateProfile() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val username = usernameEditText.text.toString().trim()
            val title = titleEditText.text.toString().trim()

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("userProfile").document(userId)

            val selectedUri = uri // Assign the value of uri to a temporary variable
            val previousImageUrl = intent.getStringExtra("previousImageUrl")

            if (selectedUri != null) {
                val imageRef = storageRef.child("images").child(System.currentTimeMillis().toString())

                imageRef.putFile(selectedUri)
                    .addOnSuccessListener { uploadTask ->
                        uploadTask.storage.downloadUrl
                            .addOnSuccessListener { downloadUri ->
                                val imageUrl = downloadUri.toString()

                                val updatedProfile = hashMapOf(
                                    "username" to username,
                                    "title" to title,
                                    "image_url" to imageUrl
                                )

                                userRef.set(updatedProfile)
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { error ->
                                        Toast.makeText(this, "Failed to update profile: ${error.message}", Toast.LENGTH_SHORT).show()
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
                // If no new image is selected, update only the username and title
                val updatedProfile = hashMapOf(
                    "username" to username,
                    "title" to title,
                    "image_url" to previousImageUrl // Set the previous image URL
                )

                userRef.set(updatedProfile)
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { error ->
                        Toast.makeText(this, "Failed to update profile: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }


}
