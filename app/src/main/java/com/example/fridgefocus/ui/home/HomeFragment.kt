package com.example.fridgefocus.ui.home

import Item
import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.fridgefocus.R
import com.example.fridgefocus.databinding.FragmentHomeBinding
import java.io.File
import java.io.IOException
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Environment
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgefocus.ItemAdapter
import com.example.fridgefocus.ItemSpacingDecoration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val CAMERA_REQUEST_CODE = 1001
    private var photoUri: Uri? = null

    private lateinit var editName: EditText
    private lateinit var editQuantity: EditText
    private lateinit var editUnit: EditText

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    
    // Replace the local list with a ViewModel
    private lateinit var homeViewModel: HomeViewModel
    
    private lateinit var add_button: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize the ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        
        // Inflate the layout
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = inflater.inflate(R.layout.fragment_home, container, false)
        
        // Set up RecyclerView
        recyclerView = view.findViewById(R.id.ingredient_list)
        recyclerView.addItemDecoration(ItemSpacingDecoration(16))
        
        // Initialize with empty list, will be populated from ViewModel
        adapter = ItemAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter
        
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Set up button click listener
        add_button = view.findViewById(R.id.ingredients_add_btn)
        add_button.setOnClickListener {
            showEditDialog()
        }
        
        // Set up observers for ViewModel data
        setupObservers()
        
        // Load inventory data when fragment is created
        homeViewModel.loadInventory()
    }
    
    private fun setupObservers() {
        // Observe inventory items
        homeViewModel.inventoryItems.observe(viewLifecycleOwner) { items ->
            // Convert Inventory objects to Item objects for the adapter
            val adapterItems = items.map { inventory ->
                Item(
                    inventory.name,
                    inventory.quantity.toIntOrNull() ?: 0, 
                    inventory.unit
                )
            }
            
            // Update the RecyclerView
            adapter = ItemAdapter(adapterItems.toMutableList())
            recyclerView.adapter = adapter
        }
        
        // Observe loading state (optional)
        homeViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Show loading indicator if needed
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
        
        // Observe error state
        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe success messages
        homeViewModel.successMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showEditDialog(){
        val dialogView = layoutInflater.inflate(R.layout.ingredients_add_dialog, null)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Close button
        dialogView.findViewById<ImageButton>(R.id.ingredients_additem_btnclose).setOnClickListener {
            dialog.dismiss() // Close dialog when clicked
        }

        // Edit button
        dialogView.findViewById<ImageButton>(R.id.btnEdit).setOnClickListener {
            // Handle edit action
            showAddDialog()
            dialog.dismiss()
        }

        // Camera button
        dialogView.findViewById<ImageButton>(R.id.btnCamera).setOnClickListener {
            dialog.dismiss() // Optional: close the dialog when opening camera
        }

        dialog.show()

        dialog.window?.setLayout(700, 270)
    }

    private fun showAddDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialoginputbinding, null)

        editName = dialogView.findViewById(R.id.ingredient_edit_name)
        editQuantity = dialogView.findViewById(R.id.ingredient_edit_quanitity)
        editUnit = dialogView.findViewById(R.id.ingredient_edit_unit)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Info")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                val name = editName.text.toString()
                val quantity = editQuantity.text.toString()
                val unit = editUnit.text.toString()
                
                if (name.isNotBlank() && quantity.isNotBlank() && unit.isNotBlank()) {
                    // Add to database via ViewModel
                    homeViewModel.addInventoryItem(name, quantity, unit)
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}