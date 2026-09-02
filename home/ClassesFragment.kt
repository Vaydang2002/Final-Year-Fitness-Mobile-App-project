package com.example.personalisedfitnessmobileapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.util.LocalHealthManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ClassesFragment : Fragment() {

    private val homeViewModel: HomeViewModel by activityViewModels()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var localManager: LocalHealthManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.classes, container, false)
        localManager = LocalHealthManager(requireContext())

        val btnCardio = view.findViewById<Button>(R.id.button2)
        val tvCardioSpots = view.findViewById<TextView>(R.id.tvCardioSpots)

        val btnStrength = view.findViewById<Button>(R.id.button3)
        val tvStrengthSpots = view.findViewById<TextView>(R.id.tvStrengthSpots)

        val btnBoxing = view.findViewById<Button>(R.id.button4)
        val tvBoxingSpots = view.findViewById<TextView>(R.id.tvBoxingSpots)

        val btnYoga = view.findViewById<Button>(R.id.button5)
        val tvYogaSpots = view.findViewById<TextView>(R.id.tvYogaSpots)

        // Observe Cardio
        homeViewModel.cardioSpots.observe(viewLifecycleOwner) { spots ->
            tvCardioSpots.text = "$spots/20 spots left"
        }
        homeViewModel.isCardioBooked.observe(viewLifecycleOwner) { isBooked ->
            if (isBooked) {
                btnCardio.text = "Booked"
            } else {
                btnCardio.text = "Book Now"
            }
        }

        // Observe Strength
        homeViewModel.strengthSpots.observe(viewLifecycleOwner) { spots ->
            tvStrengthSpots.text = "$spots/20 spots left"
        }
        homeViewModel.isStrengthBooked.observe(viewLifecycleOwner) { isBooked ->
            if (isBooked) {
                btnStrength.text = "Booked"
            } else {
                btnStrength.text = "Book Now"
            }
        }

        // Observe Boxing
        homeViewModel.boxingSpots.observe(viewLifecycleOwner) { spots ->
            tvBoxingSpots.text = "$spots/20 spots left"
        }
        homeViewModel.isBoxingBooked.observe(viewLifecycleOwner) { isBooked ->
            if (isBooked) {
                btnBoxing.text = "Booked"
            } else {
                btnBoxing.text = "Book Now"
            }
        }

        // Observe Yoga
        homeViewModel.yogaSpots.observe(viewLifecycleOwner) { spots ->
            tvYogaSpots.text = "$spots/20 spots left"
        }
        homeViewModel.isYogaBooked.observe(viewLifecycleOwner) { isBooked ->
            if (isBooked) {
                btnYoga.text = "Booked"
            } else {
                btnYoga.text = "Book Now"
            }
        }

        btnCardio.setOnClickListener {
            handleBooking("HIIT Cardio", homeViewModel.isCardioBooked.value ?: false, homeViewModel.cardioSpots.value ?: 20) {
                homeViewModel.bookClass("Cardio")
            }
        }

        btnStrength.setOnClickListener {
            handleBooking("Strength & Conditioning", homeViewModel.isStrengthBooked.value ?: false, homeViewModel.strengthSpots.value ?: 20) {
                homeViewModel.bookClass("Strength")
            }
        }

        btnBoxing.setOnClickListener {
            handleBooking("Boxing", homeViewModel.isBoxingBooked.value ?: false, homeViewModel.boxingSpots.value ?: 20) {
                homeViewModel.bookClass("Boxing")
            }
        }

        btnYoga.setOnClickListener {
            handleBooking("Yoga Flow", homeViewModel.isYogaBooked.value ?: false, homeViewModel.yogaSpots.value ?: 20) {
                homeViewModel.bookClass("Yoga")
            }
        }

        return view
    }

    private fun handleBooking(className: String, isAlreadyBooked: Boolean, spotsLeft: Int, onConfirm: () -> Unit) {
        if (isAlreadyBooked) {
            AlertDialog.Builder(requireContext())
                .setTitle("Already Booked")
                .setMessage("You have already booked a slot for $className. You cannot book the same class twice.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        if (spotsLeft <= 0) {
            Toast.makeText(context, "No spots left for $className!", Toast.LENGTH_SHORT).show()
            return
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Confirm Booking")
            .setMessage("Are you sure you want to book $className?")
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
                saveBookingToFirestore()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun saveBookingToFirestore() {
        val userId = auth.currentUser?.uid ?: return
        // The booking status is already updated via homeViewModel.bookClass() which updates Firestore.
        // We just show a confirmation toast here.
        Toast.makeText(context, "Booking confirmed!", Toast.LENGTH_SHORT).show()
    }
}