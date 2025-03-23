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
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.telecom.Call
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.crashlytics.buildtools.reloc.com.google.common.net.MediaType
import java.io.FileOutputStream
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


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var add_button:Button

    private lateinit var photoButton: Button
    private lateinit var photoPreview: ImageView

    private lateinit var openDialogButton: Button
    private var currentPhotoPath: String = ""
    private var photoUri: Uri? = null

    // Camera result launcher
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Photo was taken successfully
            savePhotoToAppStorage()
            Toast.makeText(requireContext(), "Photo saved successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Permission launcher
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            dispatchTakePictureIntent()
        } else {
            Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

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
            // Handle camera action
            // Check for camera permission
//            checkCameraPermission()
            // Check for camera permission
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // We have permission, launch camera
                dispatchTakePictureIntent()
            } else {
                // Request camera permission
                requestPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
            dialog.dismiss() // Optional: close the dialog when opening camera
        }

        dialog.show()

        dialog.window?.setLayout(700, 270)

    }

    private fun dispatchTakePictureIntent() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Ensure there's a camera activity to handle the intent
        takePictureIntent.resolveActivity(requireActivity().packageManager)?.also {
            // Create the file where the photo should go
            val photoFile: File? = try {
                createTempImageFile()
            } catch (ex: IOException) {
                Toast.makeText(requireContext(), "Error creating image file", Toast.LENGTH_SHORT).show()
                null
            }

            // Continue only if the file was successfully created
            photoFile?.also {
                photoUri = FileProvider.getUriForFile(
                    requireContext(),
                    "${requireContext().packageName}.fileprovider",
                    it
                )
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                takePictureLauncher.launch(takePictureIntent)
            }
        } ?: run {
            Toast.makeText(requireContext(), "No camera app found", Toast.LENGTH_SHORT).show()
        }
    }

    @Throws(IOException::class)
    private fun createTempImageFile(): File {
        // Create an image file name
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        val storageDir = requireContext().cacheDir

        return File.createTempFile(
            imageFileName, /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save the path for later
            currentPhotoPath = absolutePath
        }
    }

    private fun savePhotoToAppStorage() {
        try {
            // Get source file
            val sourceFile = File(currentPhotoPath)

            // Get app's root directory for saving
            val appDir = requireContext().filesDir

            // Create unique filename
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val destinationFile = File(appDir, "Photo_$timeStamp.jpg")

            // Copy file to app storage
            sourceFile.inputStream().use { input ->
                FileOutputStream(destinationFile).use { output ->
                    input.copyTo(output)
                }
            }

            // Delete temp file
            sourceFile.delete()

            // Optional: Notify activity about the photo save
            (activity as? PhotoListener)?.onPhotoSaved(destinationFile.absolutePath)

        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error saving photo: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Interface for communicating with the activity if needed
    interface PhotoListener {
        fun onPhotoSaved(photoPath: String)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}