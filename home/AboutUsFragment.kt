package com.example.personalisedfitnessmobileapplication.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalisedfitnessmobileapplication.R

class AboutUsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.about_us, container, false)

        // Initialize Chat UI
        val recyclerView = view.findViewById<RecyclerView>(R.id.chatRecyclerView)
        val etMessage = view.findViewById<EditText>(R.id.etMessage)
        val btnSend = view.findViewById<ImageButton>(R.id.btnSend)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        
        btnSend?.setOnClickListener {
            val message = etMessage.text.toString()
            if (message.isNotEmpty()) {
                Toast.makeText(context, "FitBot: Processing...", Toast.LENGTH_SHORT).show()
                etMessage.text.clear()
            }
        }

        return view
    }
}