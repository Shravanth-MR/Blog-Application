package com.example.blog_app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.blog_app.Login
import com.example.blog_app.R

class StartActivity: AppCompatActivity(), View.OnClickListener {

    private lateinit var startProjectButton: Button
    private lateinit var quitButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)
        supportActionBar?.title = "Mobile Application Development"

        startProjectButton = findViewById(R.id.startProjectButton)
        quitButton = findViewById(R.id.quitButton)

        startProjectButton.setOnClickListener(this)
        quitButton.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.startProjectButton -> {
                // Start the Login activity
                val intent = Intent(this, Login::class.java)
                startActivity(intent)
            }
            R.id.quitButton -> {
                // Close the app
                finishAffinity()
            }
        }
    }
}
