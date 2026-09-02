package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.R
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class EnterNewPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()
    private var resetCode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.confirm_password)

        auth = FirebaseAuth.getInstance()

        // Catch the 'oobCode' from the email link if the app is opened via App Link
        intent.data?.let { resetCode = it.getQueryParameter("oobCode") }

        val btnContinue = findViewById<Button>(R.id.btnConfirmLogin)
        val etNewPassword = findViewById<TextInputEditText>(R.id.etNewPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)

        btnContinue.setOnClickListener {
            val newPass = etNewPassword.text.toString().trim()
            val confirmPass = etConfirmPassword.text.toString().trim()

            if (newPass.isEmpty()) {
                etNewPassword.error = "Password is required"
                return@setOnClickListener
            }

            if (newPass.length < 6) {
                etNewPassword.error = "Password should be at least 6 characters"
                return@setOnClickListener
            }

            if (newPass != confirmPass) {
                etConfirmPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            if (resetCode != null) {
                auth.confirmPasswordReset(resetCode!!, newPass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // After password reset, we don't have the userId immediately because we are logged out.
                        // We will add the notification after they log in next time, OR try to find user by email.
                        // For simplicity, we just notify "Success" and move to login.
                        Toast.makeText(this, "Password Updated Successfully!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, LoginActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Reset link expired or invalid", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Invalid reset link. Please use the link sent to your email.", Toast.LENGTH_LONG).show()
            }
        }
    }
}