package com.example.personalisedfitnessmobileapplication.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R

class EditProfileActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.edit_profile)

        val btnBack = findViewById<ImageButton>(R.id.btnBackEditProfile)
        val btnSave = findViewById<Button>(R.id.btnSaveProfile)
        
        btnBack.setOnClickListener {
            finish()
        }
        
        btnSave.setOnClickListener {
            // Logic to save profile changes would go here
            Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}