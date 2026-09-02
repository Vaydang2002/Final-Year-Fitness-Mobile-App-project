package com.example.personalisedfitnessmobileapplication.ui.health

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.model.HealthTracking
import com.example.personalisedfitnessmobileapplication.ui.health.HealthRecordsFragment
import com.example.personalisedfitnessmobileapplication.ui.home.HomeActivity
import com.example.personalisedfitnessmobileapplication.util.LocalHealthManager
import com.google.android.material.textfield.TextInputEditText
import java.util.UUID

class HealthFragment : Fragment() {

    private lateinit var etWeight: TextInputEditText
    private lateinit var etHeight: TextInputEditText
    private lateinit var etGoals: TextInputEditText
    private lateinit var btnAddHealth: Button

    private lateinit var etExerciseName: TextInputEditText
    private lateinit var etEquipment: TextInputEditText
    private lateinit var etWorkoutWeight: TextInputEditText
    private lateinit var etSets: TextInputEditText
    private lateinit var etReps: TextInputEditText
    private lateinit var btnAddWorkout: Button

    private lateinit var btnViewRecords: Button
    private lateinit var btnSaveAll: Button

    private lateinit var localManager: LocalHealthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.health, container, false)
        localManager = LocalHealthManager(requireContext())

        // Vitals Views
        etWeight = view.findViewById(R.id.etWeight)
        etHeight = view.findViewById(R.id.etHeight)
        etGoals = view.findViewById(R.id.etGoals)
        btnAddHealth = view.findViewById(R.id.HealthAddBttn)

        // Workout Views
        etExerciseName = view.findViewById(R.id.etExerciseName)
        etEquipment = view.findViewById(R.id.etEquipment)
        etWorkoutWeight = view.findViewById(R.id.etWorkoutWeight)
        etSets = view.findViewById(R.id.etSets)
        etReps = view.findViewById(R.id.etReps)
        btnAddWorkout = view.findViewById(R.id.btnAddWorkout)

        btnViewRecords = view.findViewById(R.id.btnViewRecords)
        btnSaveAll = view.findViewById(R.id.btnSaveAll)

        btnAddHealth.setOnClickListener { saveHealthData() }
        btnAddWorkout.setOnClickListener { saveWorkoutData() }
        btnSaveAll.setOnClickListener { saveEverything() }
        btnViewRecords.setOnClickListener {
            (activity as? HomeActivity)?.replaceFragment(HealthRecordsFragment())
        }

        return view
    }

    private fun saveEverything() {
        val weight = etWeight.text.toString().trim()
        val exercise = etExerciseName.text.toString().trim()
        if (weight.isNotEmpty()) saveHealthData()
        if (exercise.isNotEmpty()) saveWorkoutData()
        if (weight.isEmpty() && exercise.isEmpty()) {
            Toast.makeText(context, "Enter data to save", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveHealthData() {
        val weight = etWeight.text.toString().trim()
        val height = etHeight.text.toString().trim()
        val goals = etGoals.text.toString().trim()

        if (weight.isEmpty() || height.isEmpty()) {
            Toast.makeText(context, "Weight and Height required", Toast.LENGTH_SHORT).show()
            return
        }

        val record = HealthTracking(
            id = UUID.randomUUID().toString(),
            type = "vitals",
            weight = weight,
            height = height,
            goals = goals,
            isCompleted = false
        )

        localManager.saveRecord(record)
        Toast.makeText(context, "Vitals saved locally!", Toast.LENGTH_SHORT).show()
        clearVitalsFields()
    }

    private fun saveWorkoutData() {
        val name = etExerciseName.text.toString().trim()
        val sets = etSets.text.toString().trim()
        val reps = etReps.text.toString().trim()

        if (name.isEmpty() || sets.isEmpty()) {
            Toast.makeText(context, "Name and Sets required", Toast.LENGTH_SHORT).show()
            return
        }

        val record = HealthTracking(
            id = UUID.randomUUID().toString(),
            type = "workout",
            exerciseName = name,
            equipment = etEquipment.text.toString().trim(),
            workoutWeight = etWorkoutWeight.text.toString().trim(),
            sets = sets,
            reps = reps
        )

        localManager.saveRecord(record)
        Toast.makeText(context, "Workout logged locally!", Toast.LENGTH_SHORT).show()
        clearWorkoutFields()
    }

    private fun clearVitalsFields() {
        etWeight.text?.clear(); etHeight.text?.clear(); etGoals.text?.clear()
    }

    private fun clearWorkoutFields() {
        etExerciseName.text?.clear(); etEquipment.text?.clear(); etWorkoutWeight.text?.clear(); etSets.text?.clear(); etReps.text?.clear()
    }
}