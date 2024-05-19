package com.example.petcareproject.views.authview

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.finishAffinity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.petcareproject.databinding.FragmentVetRegisterBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodels.AuthViewModel

class VetRegisterFragment : Fragment() {
    private var _binding: FragmentVetRegisterBinding? = null
    private val binding get() = _binding!!
    private val TAG = "VetAuth"
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }
    private val FILE_REQUEST_CODE = 1
    private val PERMISSION_REQUEST_CODE = 2
    private var fileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vetIdAttachButton = binding.attachVetIdButton
        val registerButton = binding.registerButton
        val fileCancelButton = binding.fileCancelButton


        vetIdAttachButton.setOnClickListener {
            // Check if permission is granted, if not, request permission

            if (checkPermission()) {
                println("Already granted")

                // Permission already granted, start file picker
                startFilePicker()
            } else {
                println("Asking")

                // Permission not granted, request permission
                requestPermission()
            }
        }
        registerButton.setOnClickListener {
            val user = VetRegisterFragmentArgs.fromBundle(requireArguments()).vetUser
            Log.d(TAG, "BUTTON URI: ${fileUri}")
            Log.d(TAG, "BUTTON USER: ${user}")
            if (fileUri != null) {
                viewModel.register(
                    email = user.userEmail,
                    password = user.userPassword,
                    fullName = user.userName,
                    isVet = true,
                    registeredVetClinic = binding.registeredVetClinic.text.toString(),
                    vetUri = fileUri
                )
                showConfirmationDialog()
            } else {
                // Show toast message
                Toast.makeText(requireContext(), "Please upload a file to complete registration.", Toast.LENGTH_SHORT).show()
            }
        }
        fileCancelButton.setOnClickListener {
            // Clear the fileUri
            fileUri = null
            // Show toast message
            Toast.makeText(requireContext(), "File selection canceled.", Toast.LENGTH_SHORT).show()
            binding.vetAttachedFileName.visibility = View.INVISIBLE
            binding.fileCancelButton.visibility = View.INVISIBLE
        }
    }
    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun startFilePicker() {
        // Launch file picker intent
        println("Launch file picker")
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "*/*"
        startActivityForResult(intent, FILE_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start file picker
                startFilePicker()
            }
        }
    }
    private fun showConfirmationDialog() {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Your registration information will be checked in 2 business days and an email will be sent to your email address once it is completed. Until then, you will not be able to use your account. Thank you for your patience.")
            .setCancelable(false)
            .setPositiveButton("Close App", DialogInterface.OnClickListener { dialog, id ->
                // Close the app
                finishAffinity(requireActivity())
            })
        val alert = dialogBuilder.create()
        alert.setTitle("Registration Confirmation")
        alert.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            if (uri != null) {
                val fileName = getFileName(uri)
                if (fileName.isNotEmpty()) {
                    println(fileName)
                    binding.vetAttachedFileName.text = fileName
                    binding.vetAttachedFileName.visibility = View.VISIBLE
                    binding.fileCancelButton.visibility = View.VISIBLE
                    fileUri = uri // Make sure to update fileUri
                    Log.d(TAG, "RESULT URI: ${fileUri}")

                } else {
                    Toast.makeText(requireContext(), "Failed to get file name, please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "File selection failed, please try again.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "You didn't pick a file.", Toast.LENGTH_SHORT).show()
        }
    }


    private fun getFileName(uri: Uri?): String {
        var fileName = ""
        Log.d(TAG, "FILE NAME URI: ${uri}")
        uri?.let {
            requireContext().contentResolver.query(it, null, null, null, null)?.use { cursor ->
                val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (displayNameIndex != -1 && cursor.moveToFirst()) {
                    fileName = cursor.getString(displayNameIndex)
                }
            }
        }
        Log.d(TAG, "FILE NAME FILE NAME: ${fileName}")
        return fileName
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}