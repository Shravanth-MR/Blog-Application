package com.example.blog_app

import java.util.Date

//import androidx.appcompat.app.AppCompatActivity
//import android.os.Bundle
//
//class Blog : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_blog)
//    }
//}
data class Blog(
    val blogId: String? = null,
    val userId: String= "",
    val title: String? = null,
    val description: String? = null,
    val timestamp: Date? = null,
    val image: String? = null,
    val profileImageUrl: String? = "",
    var username: String? = "",
    val imageUrl: String = ""
) {
    // No-argument constructor required for Firebase Firestore deserialization
//    constructor() : this("", "", "", "", null, "", "", "", "")
}
