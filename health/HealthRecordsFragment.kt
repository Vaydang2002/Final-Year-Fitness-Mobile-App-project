package com.example.personalisedfitnessmobileapplication.ui.health

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.ui.health.HealthRecordsViewModel
import java.util.Random

class HealthRecordsFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HealthRecordsAdapter
    private val viewModel: HealthRecordsViewModel by viewModels()
    private val CHANNEL_ID = "goal_notifications"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.health_records, container, false)

        recyclerView = view.findViewById(R.id.rvHealthRecords)
        recyclerView.layoutManager = LinearLayoutManager(context)

        view.findViewById<ImageButton>(R.id.btnBackToTracker).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        createNotificationChannel()
        setupObservers()

        return view
    }

    private fun setupObservers() {
        // Observe records list
        viewModel.records.observe(viewLifecycleOwner) { records ->
            adapter = HealthRecordsAdapter(
                records,
                onDelete = { record -> viewModel.deleteRecord(record.id) },
                onUpdateStatus = { record, isChecked ->
                    viewModel.updateGoalStatus(
                        record.id,
                        isChecked
                    )
                }
            )
            recyclerView.adapter = adapter

            if (records.isEmpty()) {
                Toast.makeText(context, "No local records found", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe goal completion events (Notifications)
        viewModel.goalClearedEvent.observe(viewLifecycleOwner) { message ->
            message?.let {
                sendGoalCompletionNotification(it)
                Toast.makeText(context, "Goal marked as Passed/Cleared!", Toast.LENGTH_SHORT).show()
                viewModel.clearGoalEvent()
            }
        }
    }

    private fun sendGoalCompletionNotification(message: String) {
        val context = context ?: return
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.baseline_notifications_24)
            .setContentTitle("Goal Cleared!")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        notificationManager.notify(Random().nextInt(), builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Goal Notifications"
            val descriptionText = "Notifications for completing fitness goals"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}