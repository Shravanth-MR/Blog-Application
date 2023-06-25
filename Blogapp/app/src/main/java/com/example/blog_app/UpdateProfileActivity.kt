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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
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

    private lateinit var auth: FirebaseAuth

    private lateinit var storageRef: StorageReference
    private var uri: Uri? = null

    private lateinit var galleryImageLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_profile)

        auth = Firebase.auth

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

        val deleteAccountButton: Button = findViewById(R.id.deleteAccountButton)
        deleteAccountButton.setOnClickListener {
            deleteAccount()
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

    private fun deleteAccount() {
        val user = auth.currentUser

        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("Are you sure you want to delete your account?")
            .setPositiveButton("Yes") { dialog, _ ->
                val userId = user?.uid
                if (userId != null) {
                    // Delete user data from "blogs" collection
                    val db = FirebaseFirestore.getInstance()
                    val blogsRef = db.collection("blogs")
                    blogsRef.whereEqualTo("userId", userId)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot.documents) {
                                document.reference.delete()
                            }
                        }
                        .addOnFailureListener { error ->
                            Toast.makeText(this@UpdateProfileActivity, "Failed to delete user's blog entries: ${error.message}", Toast.LENGTH_SHORT).show()
                        }

                    // Delete user profile data from "userProfile" collection
                    val userImagesRef = db.collection("userProfile")
                    userImagesRef.document(userId)
                        .delete()
                        .addOnFailureListener { error ->
                            Toast.makeText(this@UpdateProfileActivity, "Failed to delete user profile: ${error.message}", Toast.LENGTH_SHORT).show()
                        }

                    // Delete user account
                    user.delete()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // Account deleted successfully
                                Toast.makeText(this@UpdateProfileActivity, "Account deleted.", Toast.LENGTH_SHORT).show()
                                // Redirect to the login page
                                val intent = Intent(this@UpdateProfileActivity, Login::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            } else {
                                // Failed to delete the account
                                Toast.makeText(this@UpdateProfileActivity, "Failed to delete account.", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.setTitle("Confirmation")
        alert.show()
    }




    private fun loadImageWithGlide(imageUrl: String?, placeholderImage: Int) {
        val requestOptions = RequestOptions.circleCropTransform()
            .placeholder(placeholderImage)
            .error(placeholderImage)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .apply(requestOptions)
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
