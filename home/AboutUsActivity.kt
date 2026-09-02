package com.example.personalisedfitnessmobileapplication.ui.home

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R

class AboutUsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.about_us)

        val btnBack = findViewById<ImageButton>(R.id.imageButton9) // Matching ID in about_us.xml
        btnBack?.setOnClickListener {
            finish()
        }
    }
}