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
import android.telecom.Call
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fridgefocus.ItemAdapter
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.MediaType
//import okhttp3.MediaType
//import okhttp3.MultipartBody
//import okhttp3.RequestBody
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val CAMERA_REQUEST_CODE = 1001
    private var photoUri: Uri? = null

    private lateinit var editName: EditText
    private lateinit var editQuantity:EditText
    private lateinit var editUnit:EditText

    private var name : String = ""
    private var quantity : Int = -1
    private var unit : String = ""

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ItemAdapter
    private val items = mutableListOf<Item>()   //this one will be deleted later

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!
    private lateinit var add_button:Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val view = inflater.inflate(R.layout.fragment_home,container,false)
        recyclerView = view.findViewById(R.id.ingredient_list)

        adapter = ItemAdapter(items)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_button = view.findViewById(R.id.ingredients_add_btn)
        add_button.setOnClickListener {
            showEditDialog()  // Show the dialog when the button is clicked
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

    //this is the edit dialog for adding items
    private fun showAddDialog(){
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialoginputbinding, null)

        editName = dialogView.findViewById(R.id.ingredient_edit_name)
        editQuantity = dialogView.findViewById(R.id.ingredient_edit_quanitity)
        editUnit = dialogView.findViewById(R.id.ingredient_edit_unit)

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Info")
            .setView(dialogView)
            .setPositiveButton("OK") { _, _ ->
                name = editName.text.toString()
                quantity = editQuantity.text.toString().toIntOrNull() ?: -1// default -1 if input is invalid
                unit = editUnit.text.toString()
                // Save or use variables here
                    Log.d("DialogResult", "Name: $name, Quantity: $quantity, Unit: $unit")


                //adding the item
                items.add(Item(name,quantity, unit))

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