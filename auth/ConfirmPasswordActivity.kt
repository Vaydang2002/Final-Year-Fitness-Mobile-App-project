package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.home.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class ConfirmPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_password)

        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmLogin)

        // Get email from previous intent
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        btnBack.setOnClickListener { finish() }

        btnConfirm.setOnClickListener {
            val password = etNewPassword.text.toString().trim()

            if (password.isEmpty()) {
                etNewPassword.error = "Please enter your new password"
                return@setOnClickListener
            }

            // Attempt to login with the new password for security verification
            auth.signInWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Security Verified!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, HomeActivity::class.java)
                        startActivity(intent)
                        finishAffinity()
                    } else {
                        Toast.makeText(this, "Verification failed. Check your new password.", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}