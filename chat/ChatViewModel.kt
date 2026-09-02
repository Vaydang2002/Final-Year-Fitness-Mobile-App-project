package com.example.personalisedfitnessmobileapplication.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.personalisedfitnessmobileapplication.data.api.GeminiApiService
import com.example.personalisedfitnessmobileapplication.data.model.Content
import com.example.personalisedfitnessmobileapplication.data.model.GeminiRequest
import com.example.personalisedfitnessmobileapplication.data.model.Part
import com.example.personalisedfitnessmobileapplication.model.Customer
import com.example.personalisedfitnessmobileapplication.model.Goal
import com.example.personalisedfitnessmobileapplication.model.Workout
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ChatViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val apiKey = "YOUR_GEMINI_API_KEY" // Replace with your actual Gemini API Key

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(GeminiApiService::class.java)

    private suspend fun getUserContext(userId: String): String {
        return try {
            val customer = db.collection("CustomerCollection")
                .document(userId).get().await().toObject(Customer::class.java)

            val workouts = db.collection("Workouts Collection")
                .get().await().toObjects(Workout::class.java)

            val goals = db.collection("Goals")
                .whereEqualTo("customerId", userId)
                .get().await().toObjects(Goal::class.java)

            """
        User Info:
        Name: ${customer?.customerName}
        Goal: ${customer?.fitnessGoal}
        Membership: ${customer?.membershipStatus}

        Workouts:
        ${workouts.joinToString { it.workout + " (" + it.level + ") for " + it.duration + " mins" }}

        Goals:
        ${goals.joinToString { it.goalType + ": " + it.progress }}
        """.trimIndent()

        } catch (e: Exception) {
            "No user data available"
        }
    }

    fun sendMessage(userId: String, userMessage: String) {
        viewModelScope.launch {

            val context = getUserContext(userId)

            val fullPrompt = """
            You are FitBot, a fitness assistant.
            
            Use this user data to provide personalized advice:
            $context
            
            Answer the user question clearly and concisely.
            
            User: $userMessage
        """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(Content(listOf(Part(fullPrompt))))
            )

            try {
                val response = api.sendMessage(request, apiKey)
                val reply = response.candidates[0].content.parts[0].text

                // TODO: Save message to Firestore or update UI State
                println("FitBot: $reply")
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}