package com.example.blog_app


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class Navhome : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var auth: FirebaseAuth
    private lateinit var logoutButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navhome)

        val createBlogButton: FloatingActionButton = findViewById(R.id.createBlogButton)
        createBlogButton.setOnClickListener {
            // Handle create blog button click here
            // For example, show a toast message or perform any desired action
//            Toast.makeText(this, "Create Blog button clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, Blogpost::class.java)
            startActivity(intent)
        }


        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_home -> {
                    // Handle home item selection
                    // For example, navigate to the HomeFragment
                    navigateToHomeFragment()
                    true
                }
                R.id.action_explore -> {
                    // Handle explore item selection
                    // For example, navigate to the ExploreFragment
                    navigateToExploreFragment()
                    true
                }
                R.id.action_profile -> {
                    // Handle profile item selection
                    // For example, navigate to the ProfileFragment
                    val intent = Intent(this, Profpage::class.java)
                    startActivity(intent)
                    true
                }
                else -> false
            }
        }
        auth = Firebase.auth
        logoutButton = findViewById(R.id.logout_button)

        logoutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            //hello
        }
    }

    // Functions to handle navigation to respective fragments
    private fun navigateToHomeFragment() {
        // Navigate to the HomeFragment
    }

    private fun navigateToExploreFragment() {
        // Navigate to the ExploreFragment
    }

//    private fun navigateToProfileFragment() {
//        // Navigate to the ProfileFragment
//    }
}
