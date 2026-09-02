package com.example.personalisedfitnessmobileapplication.ui.health

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.personalisedfitnessmobileapplication.model.HealthTracking
import com.example.personalisedfitnessmobileapplication.model.Notification
import com.example.personalisedfitnessmobileapplication.util.LocalHealthManager
import com.google.gson.Gson
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRecordsViewModel(application: Application) : AndroidViewModel(application) {
    private val localManager = LocalHealthManager(application)

    private val _records = MutableLiveData<List<HealthTracking>>()
    val records: LiveData<List<HealthTracking>> get() = _records

    private val _goalClearedEvent = MutableLiveData<String?>()
    val goalClearedEvent: LiveData<String?> get() = _goalClearedEvent

    init {
        fetchRecords()
    }

    fun fetchRecords() {
        _records.value = localManager.getAllRecords()
    }

    fun deleteRecord(recordId: String) {
        localManager.deleteRecord(recordId)
        fetchRecords()
    }

    fun updateGoalStatus(recordId: String, isCompleted: Boolean) {
        val allRecords = localManager.getAllRecords().toMutableList()
        val index = allRecords.indexOfFirst { it.id == recordId }

        if (index != -1) {
            allRecords[index].isCompleted = isCompleted

            // Save updated list
            saveAllLocally(allRecords)

            if (isCompleted) {
                val message = "Congratulations for clearing your goal! Now choose something harder to keep progressing."

                // Save to local notification history
                val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
                val timestamp = sdf.format(Date())
                val newNotif = Notification(
                    title = "Goal Cleared!",
                    message = message,
                    timestamp = timestamp,
                    createdAt = System.currentTimeMillis()
                )
                localManager.saveNotification(newNotif)

                // Trigger notification in Fragment
                _goalClearedEvent.value = message
            }

            fetchRecords() // Refresh the list
        }
    }

    private fun saveAllLocally(records: List<HealthTracking>) {
        val context = getApplication<Application>().applicationContext
        val fileName = "local_health_records.json"
        val gson = Gson()
        val json = gson.toJson(records)
        context.openFileOutput(fileName, Context.MODE_PRIVATE).use {
            it.write(json.toByteArray())
        }
    }

    fun clearGoalEvent() {
        _goalClearedEvent.value = null
    }
}