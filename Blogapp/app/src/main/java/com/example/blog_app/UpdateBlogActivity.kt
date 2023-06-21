package com.example.blog_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso

class UpdateBlogActivity : AppCompatActivity() {

    private lateinit var titleEditText: EditText
    private lateinit var descriptionEditText: EditText
    private lateinit var updateButton: Button
    private lateinit var cancelButton: Button
    private lateinit var selectImageButton: Button
    private lateinit var selectedImageView: ImageView
    private var selectedImageUri: Uri? = null
    private lateinit var blogId: String

    companion object {
        private const val REQUEST_IMAGE_CHOOSER = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_update_blog)

        // Retrieve the passed data
        val previousTitle = intent.getStringExtra("previousTitle")
        val previousDescription = intent.getStringExtra("previousDescription")
        val previousImageUrl = intent.getStringExtra("previousImageUrl")

        titleEditText = findViewById(R.id.titleEditText)
        descriptionEditText = findViewById(R.id.descriptionEditText)
        updateButton = findViewById(R.id.updateButton)
        cancelButton = findViewById(R.id.cancelButton)
        selectImageButton = findViewById(R.id.selectImageButton)
        selectedImageView = findViewById(R.id.selectedImageView)



        // Set the retrieved data to the corresponding views
        titleEditText.setText(previousTitle)
        descriptionEditText.setText(previousDescription)

        if (!previousImageUrl.isNullOrEmpty()) {
            selectedImageView.visibility = View.VISIBLE
            // Load and display the previous image using a library of your choice (e.g., Picasso, Glide)
            Picasso.get().load(previousImageUrl).into(selectedImageView)
        }



        blogId = intent.getStringExtra("blogId") ?: ""

        updateButton.setOnClickListener {
            val updatedTitle = titleEditText.text.toString().trim()
            val updatedDescription = descriptionEditText.text.toString().trim()

            if (updatedTitle.isNotEmpty() && updatedDescription.isNotEmpty()) {
                updateBlog(updatedTitle, updatedDescription)
            } else {
                Toast.makeText(this, "Please enter title and description", Toast.LENGTH_SHORT).show()
            }
        }

        cancelButton.setOnClickListener {
            finish()
        }

        selectImageButton.setOnClickListener {
            openImageChooser()
        }
    }

    private fun openImageChooser() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_CHOOSER)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_IMAGE_CHOOSER && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedImageUri = uri
                selectedImageView.visibility = View.VISIBLE
                selectedImageView.setImageURI(uri)
            }
        }
    }

    private fun updateBlog(updatedTitle: String, updatedDescription: String) {
        val db = FirebaseFirestore.getInstance()
        val blogsRef = db.collection("blogs")

        val updatedBlogData = hashMapOf(
            "title" to updatedTitle,
            "description" to updatedDescription
            // Update other fields as needed
        )

        selectedImageUri?.let { imageUri ->
            uploadImage(imageUri) { imageUrl ->
                updatedBlogData["image"] = imageUrl
                updateBlogData(blogsRef, updatedBlogData as HashMap<String, Any>)

            }
        } ?: run {
            updateBlogData(blogsRef, updatedBlogData as HashMap<String, Any>)
        }
    }

    private fun uploadImage(imageUri: Uri, onSuccess: (String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("blog_images/${System.currentTimeMillis()}")

        val uploadTask = imageRef.putFile(imageUri)
        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let { error ->
                    throw error
                }
            }
            imageRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                onSuccess(downloadUri.toString())
            } else {
                // Handle image upload failure
            }
        }
    }

    private fun updateBlogData(blogsRef: CollectionReference, updatedBlogData: HashMap<String, Any>) {
        blogsRef.document(blogId)
            .update(updatedBlogData)
            .addOnSuccessListener {
                // Blog updated successfully
                finish() // Finish the activity and go back to the previous screen
            }
            .addOnFailureListener { error ->
                // Handle the failure to update the blog
            }
    }
}
