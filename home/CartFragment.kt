package com.example.personalisedfitnessmobileapplication.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.model.ShopItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.*

class CartFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var tvTotal: TextView
    private lateinit var btnCheckout: Button
    private lateinit var homeActivity: HomeActivity
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.cart, container, false)

        homeActivity = activity as HomeActivity
        recyclerView = view.findViewById(R.id.rvCartItems)
        tvTotal = view.findViewById(R.id.tvCartTotal)
        btnCheckout = view.findViewById(R.id.btnCheckout)

        recyclerView.layoutManager = LinearLayoutManager(context)
        
        setupObservers()

        btnCheckout.setOnClickListener {
            val total = homeActivity.viewModel.totalAmount.value ?: 0.0
            val membership = homeActivity.viewModel.selectedMembership.value
            
            if (total > 0) {
                // Launch PaymentActivity
                val intent = Intent(requireContext(), PaymentActivity::class.java).apply {
                    putExtra("AMOUNT", total)
                    if (membership != null) {
                        putExtra("IS_MEMBERSHIP", true)
                        putExtra("PLAN_NAME", membership.product)
                    } else {
                        putExtra("IS_MEMBERSHIP", false)
                    }
                }
                startActivity(intent)
            } else {
                Toast.makeText(context, "Cart is empty", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun setupObservers() {
        homeActivity.viewModel.cartItems.observe(viewLifecycleOwner) { cartMap ->
            updateCartList()
        }
        
        homeActivity.viewModel.selectedMembership.observe(viewLifecycleOwner) { membership ->
            updateCartList()
        }

        homeActivity.viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            tvTotal.text = "£${String.format("%.2f", total)}"
        }
    }
    
    private fun updateCartList() {
        val cartMap = homeActivity.viewModel.cartItems.value ?: emptyMap()
        val membership = homeActivity.viewModel.selectedMembership.value
        
        val displayList = mutableListOf<ShopItem>()
        
        // Add shop items
        displayList.addAll(cartMap.values.map { it.first })
        
        // Add membership if it exists
        membership?.let {
            displayList.add(it)
        }
        
        // Create map of initial quantities for the adapter
        val initialQuantities = mutableMapOf<String, Int>()
        cartMap.forEach { (id, pair) ->
            initialQuantities[id] = pair.second
        }
        // Membership is always quantity 1 for display
        membership?.let {
            initialQuantities[it.productID] = 1
        }

        val adapter = ShopAdapter(displayList, initialQuantities) { item, quantity ->
            if (item.productID == "MEMBERSHIP_ID") {
                if (quantity == 0) {
                    homeActivity.viewModel.setMembership(null)
                }
            } else {
                homeActivity.viewModel.addToCart(item, quantity)
            }
        }
        recyclerView.adapter = adapter
    }
}