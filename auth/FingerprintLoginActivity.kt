package com.example.personalisedfitnessmobileapplication.ui.auth

import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.home.HomeActivity
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver
import com.hoho.android.usbserial.driver.Ch34xSerialDriver
import com.hoho.android.usbserial.driver.ProbeTable
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.ServerSocket
import java.net.Socket

class FingerprintLoginActivity : AppCompatActivity() {

    private val viewModel: AuthViewModel by viewModels()
    private var usbPort: UsbSerialPort? = null
    private var serverSocket: ServerSocket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.fingerprint)

        val tvBackToLogin = findViewById<TextView>(R.id.tvBackToLogin)

        setupUsbSerial()       // Handles physical connection to Phone (OTG)
        setupComputerBridge()  // Handles connection to Computer (Emulator Bridge)
        observeViewModel()

        tvBackToLogin.setOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.fingerprintUser.observe(this) { userData ->
            if (userData != null) {
                Toast.makeText(this, "Welcome Back, ${userData["customerName"]}", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
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

    /**
     * This creates a network bridge so your PC can send fingerprint data
     * directly to the emulator on port 8888.
     */
    private fun setupComputerBridge() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                serverSocket = ServerSocket(8888)
                while (true) {
                    val client: Socket = serverSocket?.accept() ?: break
                    val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                    val line = reader.readLine()
                    if (line != null && line.contains("MATCH_SUCCESS")) {
                        val id = line.substringAfter("ID:").trim()
                        launch(Dispatchers.Main) {
                            viewModel.loginWithFingerprint(id)
                        }
                    }
                    client.close()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupUsbSerial() {
        val manager = getSystemService(Context.USB_SERVICE) as UsbManager
        
        val customTable = ProbeTable()
        customTable.addProduct(0x2341, 0x0043, CdcAcmSerialDriver::class.java) // Arduino Uno
        customTable.addProduct(0x1a86, 0x7523, Ch34xSerialDriver::class.java) // CH340 Clone
        
        val prober = UsbSerialProber(customTable)
        val availableDrivers = prober.findAllDrivers(manager)
        
        if (availableDrivers.isEmpty()) {
            return // Silently fail USB if not found, rely on Bridge
        }

        val driver = availableDrivers[0]
        val connection = manager.openDevice(driver.device) ?: return
        val port = driver.ports[0]

        try {
            port.open(connection)
            port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)
            usbPort = port
            
            val command = "L\n"
            usbPort?.write(command.toByteArray(), 1000)

            Thread {
                val buffer = ByteArray(1024)
                while (usbPort != null) {
                    try {
                        val len = port.read(buffer, 1000)
                        if (len > 0) {
                            val data = String(buffer, 0, len).trim()
                            if (data.contains("MATCH_SUCCESS")) {
                                val id = data.substringAfter("ID:").trim()
                                runOnUiThread {
                                    viewModel.loginWithFingerprint(id)
                                }
                            }
                        }
                    } catch (e: Exception) { break }
                }
            }.start()
        } catch (e: Exception) {
            Toast.makeText(this, "Scanner connection failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        usbPort?.close()
        usbPort = null
        try {
            serverSocket?.close()
        } catch (e: Exception) {}
    }
}
