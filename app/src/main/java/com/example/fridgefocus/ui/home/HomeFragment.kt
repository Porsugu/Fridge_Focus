package com.example.fridgefocus.ui.home

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
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

    // This property is only valid between onCreateView and
    // onDestroyView.
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
        val root: View = binding.root


        return root
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

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}