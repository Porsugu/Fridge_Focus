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
            // Handle camera action
            // Check for camera permission
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
//                openCamera()
//            } else {
//                // Request permission
//                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_REQUEST_CODE)
//            }
            dialog.dismiss() // Optional: close the dialog when opening camera
        }

        dialog.show()

        dialog.window?.setLayout(700, 270)

    }

//    private fun openCamera() {
//        // Create an Intent to capture the photo
//        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
//
//        // Check if the device has a camera app available
//        if (takePictureIntent.resolveActivity(requireContext().packageManager) != null) {
//            // Create a temporary file to store the photo
//            val photoFile: File = createImageFile()
//
//            // Get URI for the file
//            photoUri = FileProvider.getUriForFile(requireContext(), "${requireContext().packageName}.fileprovider", photoFile)
//
//            // Set the URI to the Intent
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
//
//            // Start the camera activity
//            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE)
//        }
//    }
//
//    // File to store the image
//    private lateinit var currentPhotoPath: String
//    private val REQUEST_IMAGE_CAPTURE = 1
//
//    private fun createImageFile(): File {
//        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
//        val storageDir = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
//        return File.createTempFile("JPEG_${timestamp}_", ".jpg", storageDir)
//    }
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == CAMERA_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
//            // The photo is saved at photoUri
//            photoUri?.let { uri ->
//                // You can now send the photo to the API
////                sendPhotoToApi(uri)
//            }
//        }
//    }

//    private fun sendPhotoToApi(uri: Uri) {
//        // Send the captured image to an API
//        val file = File(uri.path!!)
//        val requestBody = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
//
//        val multipartBody = MultipartBody.Part.createFormData("photo", file.name, requestBody)
//
//        val apiService = ApiClient.createService(ApiService::class.java)
//        val call = apiService.uploadPhoto(multipartBody)
//
//        call.enqueue(object : Callback<ApiResponse> {
//            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
//                if (response.isSuccessful) {
//                    // Handle success
//                    Toast.makeText(requireContext(), "Photo uploaded!", Toast.LENGTH_SHORT).show()
//                } else {
//                    // Handle failure
//                    Toast.makeText(requireContext(), "Upload failed", Toast.LENGTH_SHORT).show()
//                }
//            }
//
//            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
//                // Handle error
//                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
//            }
//        })
//    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}