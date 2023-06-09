package com.example.blog_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView

class Navhome : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navhome)

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
