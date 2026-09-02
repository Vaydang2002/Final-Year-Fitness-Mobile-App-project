package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.personalisedfitnessmobileapplication.MainActivity
import com.example.personalisedfitnessmobileapplication.R
import com.google.android.material.textfield.TextInputEditText
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber

class SignUpActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private var usbPort: UsbSerialPort? = null
    private var isFingerprintEnrolled = false
    private var enrolledFingerprintId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sign_up)

        // Initialize Views
        val btnBack = findViewById<ImageButton>(R.id.imageButton9)
        val actInitial = findViewById<AutoCompleteTextView>(R.id.actInitial)
        val etFirstName = findViewById<TextInputEditText>(R.id.etFirstName)
        val etSurname = findViewById<TextInputEditText>(R.id.etSurname)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val etPhone = findViewById<TextInputEditText>(R.id.etPhone)
        val etAge = findViewById<TextInputEditText>(R.id.etAge)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnSignUp = findViewById<Button>(R.id.btnSignUp)
        val btnFingerprint = findViewById<Button>(R.id.btnFingerprint)

        // Set up Initials Dropdown
        val initials = arrayOf("Mr.", "Ms.", "Mrs.", "Dr.", "Prof.")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, initials)
        actInitial.setAdapter(adapter)

        setupUsbSerial()
        observeViewModel()

        // Back navigation
        btnBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        btnFingerprint.setOnClickListener {
            if (usbPort == null) {
                Toast.makeText(this, "Please connect the Arduino scanner", Toast.LENGTH_SHORT).show()
                setupUsbSerial()
            } else {
                Toast.makeText(this, "Place your finger on the scanner to enroll", Toast.LENGTH_SHORT).show()
            }
        }

        // Registration Logic
        btnSignUp.setOnClickListener {
            val initial = actInitial.text.toString().trim()
            val firstName = etFirstName.text.toString().trim()
            val surname = etSurname.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val ageStr = etAge.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            // Check for missing fields
            if (initial.isEmpty() || firstName.isEmpty() || surname.isEmpty() || 
                email.isEmpty() || phone.isEmpty() || ageStr.isEmpty() || 
                password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Age Validation
            val age = ageStr.toIntOrNull()
            if (age == null) {
                etAge.error = "Please enter a valid age"
                Toast.makeText(this, "Invalid age entered", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age < 16) {
                etAge.error = "Minimum age required is 16"
                Toast.makeText(this, "You must be 16 or older to join", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Password matching check
            if (password != confirmPassword) {
                etConfirmPassword.error = "Passwords do not match"
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.signUp(
                initial, firstName, surname, email, phone, age, password,
                isFingerprintEnrolled, enrolledFingerprintId
            )
        }
    }

    private fun observeViewModel() {
        viewModel.signUpSuccess.observe(this) { success ->
            if (success) {
                Toast.makeText(this, "Account Created Successfully!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        }

        viewModel.errorMessage.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun setupUsbSerial() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        val availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager)
        if (availableDrivers.isEmpty()) return

        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device) ?: return
        val port = driver.ports[0]
        
        try {
            port.open(connection)
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            usbPort = port
            
            Thread {
                val buffer = ByteArray(1024)
                while (usbPort != null) {
                    try {
                        val len = port.read(buffer, 1000)
                        if (len > 0) {
                            val data = String(buffer, 0, len).trim()
                            if (data.contains("ENROLL_SUCCESS")) {
                                val id = data.substringAfter("ID:").trim()
                                runOnUiThread {
                                    isFingerprintEnrolled = true
                                    enrolledFingerprintId = id
                                    Toast.makeText(this, "Fingerprint Linked: $id", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } catch (e: Exception) { break }
                }
            }.start()
        } catch (e: Exception) { }
    }

    override fun onDestroy() {
        super.onDestroy()
        usbPort?.close()
        usbPort = null
    }
}