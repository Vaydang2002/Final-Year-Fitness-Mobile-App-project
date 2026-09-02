package com.example.personalisedfitnessmobileapplication.ui.home

import android.Manifest
import android.app.AlarmManager
import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.personalisedfitnessmobileapplication.MainActivity
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.health.HealthFragment
import com.example.personalisedfitnessmobileapplication.util.NotificationReceiver
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var defaultLogo: ImageView
    private lateinit var tvCartBadge: TextView
    val viewModel: HomeViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        getSharedPreferences("app_prefs", MODE_PRIVATE).edit { putBoolean("notifications_enabled", isGranted) }
        if (isGranted) scheduleReminders() else cancelAllReminders()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        auth = FirebaseAuth.getInstance()
        drawerLayout = findViewById(R.id.drawerLayout)
        defaultLogo = findViewById(R.id.imageView8)
        tvCartBadge = findViewById(R.id.tvCartBadge)
        
        setupNavigation()
        setupBadgeObserver()

        val fromLogin = intent.getBooleanExtra("FROM_LOGIN", false)
        // Only show the opt-in dialog if we haven't already granted permissions
        if (fromLogin && !isNotificationPermissionGranted()) {
            showNotificationOptInDialog()
        }

        scheduleReminders()
    }

    private fun isNotificationPermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun setupBadgeObserver() {
        viewModel.cartItems.observe(this) { cart ->
            val count = cart.size + (if (viewModel.selectedMembership.value != null) 1 else 0)
            updateCartBadge(count)
        }
        viewModel.selectedMembership.observe(this) { membership ->
            val count = (viewModel.cartItems.value?.size ?: 0) + (if (membership != null) 1 else 0)
            updateCartBadge(count)
        }
    }

    private fun updateCartBadge(count: Int) {
        if (count > 0) {
            tvCartBadge.text = count.toString()
            tvCartBadge.visibility = View.VISIBLE
        } else {
            tvCartBadge.visibility = View.GONE
        }
    }

    private fun setupNavigation() {
        val navigationView = findViewById<NavigationView>(R.id.navigationView)
        val tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        val btnCart = findViewById<ImageView>(R.id.imageView6)
        val btnNotification = findViewById<ImageButton>(R.id.notificationbtn)

        findViewById<ImageButton>(R.id.imageButton6).setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        btnCart.setOnClickListener { replaceFragment(CartFragment()) }
        btnNotification.setOnClickListener {
            startActivity(Intent(this, NotificationActivity::class.java))
        }

        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            replaceFragment(AboutUsFragment())
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.text) {
                    "About Us" -> replaceFragment(AboutUsFragment())
                    "Workout" -> replaceFragment(WorkoutFragment())
                    "Membership" -> replaceFragment(MembershipFragment())
                    "Shop" -> replaceFragment(ShopFragment())
                    "Classes" -> replaceFragment(ClassesFragment())
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_edit_profile -> startActivity(Intent(this, EditProfileActivity::class.java))
                R.id.nav_membership -> startActivity(Intent(this, MyMembershipActivity::class.java))
                R.id.nav_health -> replaceFragment(HealthFragment())
                R.id.nav_activity -> replaceFragment(GymActivityFragment())
                R.id.nav_logout -> performLogout()
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }
    }

    private fun performLogout() {
        auth.signOut()
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }

    private fun scheduleReminders() {
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!prefs.getBoolean("notifications_enabled", true)) return

        scheduleAlarm(9, 0, "Daily Motivation", "Keep moving towards your fitness goals!", 100)

        viewModel.cartItems.observe(this) { cart ->
            if (cart.isNotEmpty() || viewModel.selectedMembership.value != null) {
                scheduleAlarm(18, 0, "Cart Reminder", "You have items in your cart waiting for checkout!", 101)
            }
        }
    }

    private fun scheduleAlarm(hour: Int, minute: Int, title: String, message: String, requestCode: Int) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("message", message)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }

        if (calendar.timeInMillis < System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelAllReminders() {
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        listOf(100, 101).forEach { code ->
            val pendingIntent = PendingIntent.getBroadcast(
                this, code, Intent(this, NotificationReceiver::class.java),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            pendingIntent?.let { alarmManager.cancel(it) }
        }
    }

    private fun showNotificationOptInDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_notification_opt_in)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false)

        val btnAllow = dialog.findViewById<Button>(R.id.btnAllow)
        val btnDontAllow = dialog.findViewById<Button>(R.id.btnDontAllow)

        btnAllow.setOnClickListener {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit { putBoolean("notifications_enabled", true) }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleReminders()
            }
            dialog.dismiss()
        }

        btnDontAllow.setOnClickListener {
            getSharedPreferences("app_prefs", MODE_PRIVATE).edit { putBoolean("notifications_enabled", false) }
            cancelAllReminders()
            dialog.dismiss()
        }

        dialog.show()
    }

    fun replaceFragment(fragment: Fragment) {
        if (::defaultLogo.isInitialized) defaultLogo.visibility = View.GONE
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}