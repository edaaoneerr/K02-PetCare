package com.example.petcareproject.views.authview

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentLoginBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodels.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.login.LoginManager
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth


//bir sekilde sign out olamıyor..
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private var isNewUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        googleSignInClient = viewModel.setupGoogleSignIn(requireActivity())
        viewModel.setupFacebookSignIn(callbackManager, signIn = true)

        binding.googleButtonLayout.setOnClickListener {
            // Ensure previous Google sign-ins are cleared before starting a new sign-in attempt.
            googleSignInClient.revokeAccess().addOnCompleteListener {
                val signInIntent = googleSignInClient.signInIntent
                startActivityForResult(signInIntent, 1001) // Request code for Google SignIn
            }
        }

        binding.facebookLoginLayout.setOnClickListener {
            val permissions = listOf("email", "public_profile")
            LoginManager.getInstance().logInWithReadPermissions(this, permissions)

        }

        viewModel.loginStatus.observe(viewLifecycleOwner) { task ->
            task?.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
/*
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
*/
                    Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)

                } else {
                    authResultTask.exception?.let {
                        Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        val email = binding.emailText
        val password = binding.passwordText
        val loginButton = binding.loginButton
      viewModel.setupTextWatchers(email,
          password,
          null,
          button =  loginButton,
          context = requireContext())
        viewModel.setupObservers(email,
            password,
            context = requireContext(),
            viewLifecycleOwner = viewLifecycleOwner)


        binding.loginButton.setOnClickListener {
            val emailText = binding.emailText.text.toString()
            val passwordText = binding.passwordText.text.toString()

            var hasError = false
            if (!viewModel.validateEmail(emailText)) {
                Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_LONG).show()
                hasError = true
            }
            if (!viewModel.validatePassword(passwordText)) {
                Toast.makeText(context, "Password cannot be less than 6 characters long.", Toast.LENGTH_LONG).show()
                hasError = true
            }
            if (!hasError) {
                viewModel.login(binding.emailText.text.toString(), binding.passwordText.text.toString())
            }
        }
        binding.createAccountText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
        viewModel.showResetPasswordDialog.observe(viewLifecycleOwner) { show ->
            if (show) {
                showForgotPasswordDialog()
            }
        }


        /*viewModel.loginStatus.observe(viewLifecycleOwner) { task ->
            task?.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    authResultTask.exception?.let {
                    }
                }
            }*/


    }
    private fun showForgotPasswordDialog() {
        AlertDialog.Builder(requireContext()).apply {
            setTitle("Forgot your password?")
            setMessage("Would you like to reset your password?")
            setPositiveButton("Send Email") { dialog, which ->
                // Code to send a password reset email
                val auth = FirebaseAuth.getInstance()
                val emailAddress = binding.emailText.text.toString()
                auth.sendPasswordResetEmail(emailAddress)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                requireContext(),
                                "Email sent, please check your inbox.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            // Handle the case where sending the email fails
                            Toast.makeText(
                                requireContext(),
                                "Failed to send email.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
            }
            setNegativeButton("Try Again") { dialog, which ->
                // Code to retry authentication
                dialog.dismiss()
            }
            setCancelable(false)
            show()
        }
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        viewModel.handleGoogleSignInResult(data, requestCode)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}