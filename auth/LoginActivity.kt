package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.home.HomeActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class LoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvSignUp = findViewById<TextView>(R.id.tvSignUp)
        val btnFingerprint = findViewById<Button>(R.id.btnFingerprint)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        setupObservers()

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty()) {
                etEmail.error = "Email is required"
                etEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                etPassword.error = "Password is required"
                etPassword.requestFocus()
                return@setOnClickListener
            }

            viewModel.login(email, password)
        }

        tvSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }

        btnFingerprint.setOnClickListener {
            startActivity(Intent(this, FingerprintLoginActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
    }

    private fun setupObservers() {
        viewModel.user.observe(this) { user ->
            if (user != null) {
                addLoginNotification(user.uid)
                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                
                // Pass a flag to HomeActivity to trigger the notification opt-in
                val intent = Intent(this, HomeActivity::class.java)
                intent.putExtra("FROM_LOGIN", true)
                startActivity(intent)
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { error ->
            if (error != null) {
                Toast.makeText(this, "Authentication Failed: $error", Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun addLoginNotification(userId: String) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        val timestamp = sdf.format(Date())

        val notification = hashMapOf(
            "title" to "Security Alert",
            "message" to "New login detected on your account.",
            "timestamp" to timestamp,
            "createdAt" to System.currentTimeMillis()
        )

        db.collection("users").document(userId).collection("notifications")
            .add(notification)
    }
}