package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.home.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth

class PasswordVerificationActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.password_verification)

        auth = FirebaseAuth.getInstance()

        val btnBack = findViewById<ImageButton>(R.id.btn_back3)
        val btnVerify = findViewById<Button>(R.id.button9)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val tvResend = findViewById<TextView>(R.id.tvResend)

        // Receive email from ResetPasswordActivity
        val userEmail = intent.getStringExtra("USER_EMAIL") ?: ""

        btnBack.setOnClickListener {
            finish()
        }

        // 1. Verify and Login Logic
        btnVerify.setOnClickListener {
            val password = etNewPassword.text.toString().trim()

            if (password.isEmpty()) {
                etNewPassword.error = "Please enter your password"
                return@setOnClickListener
            }

            // Attempt to login with the new password
            auth.signInWithEmailAndPassword(userEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                        val homeIntent = Intent(this, HomeActivity::class.java)
                        startActivity(homeIntent)
                        finishAffinity() // Clear all previous activities for security
                    } else {
                        // WRONG PASSWORD CONDITION:
                        etNewPassword.error = "Incorrect password. Please re-type the password you set in the link."
                        etNewPassword.requestFocus()
                        Toast.makeText(this, "Verification failed. If you haven't clicked the link in your email yet, please do so first.", Toast.LENGTH_LONG).show()
                    }
                }
        }

        // 2. Resend Link Logic
        tvResend.setOnClickListener {
            if (userEmail.isNotEmpty()) {
                auth.sendPasswordResetEmail(userEmail)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "A new reset link has been sent to $userEmail", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Error resending: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                Toast.makeText(this, "Email address not found. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}