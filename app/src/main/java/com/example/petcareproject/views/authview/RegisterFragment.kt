package com.example.petcareproject.views.authview

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentRegisterBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.model.User
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodels.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignInClient

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient

    private lateinit var callbackManager: CallbackManager
    private var isNewUser: Boolean = false
    private val TAG = "VetAuth"
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        callbackManager = CallbackManager.Factory.create()

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        googleSignInClient = viewModel.setupGoogleSignIn(requireActivity())
        viewModel.setupFacebookSignIn(callbackManager, signIn = false)

        binding.googleButtonLayout.setOnClickListener {
            if (binding.policyCheckbox.isChecked) {
                // Ensure previous Google sign-ins are cleared before starting a new sign-in attempt.
                googleSignInClient.revokeAccess().addOnCompleteListener {
                    val signInIntent = googleSignInClient.signInIntent
                    startActivityForResult(signInIntent, 1001) // Request code for Google SignIn
                }
            } else {
                viewModel.errorLiveData.postValue("Please read and agree to Terms and Policy before sign up.")
            }

        }

        binding.facebookLoginLayout.setOnClickListener {
            if (binding.policyCheckbox.isChecked) {
                // Ensure previous Google sign-ins are cleared before starting a new sign-in attempt.
                val permissions = listOf("email", "public_profile")
                LoginManager.getInstance().logInWithReadPermissions(this, permissions)
            } else {
                viewModel.errorLiveData.postValue("Please read and agree to Terms and Policy before sign up.")
            }


        }
        val email = binding.emailRegText
        val password = binding.passwordText
        val fullName = binding.fullNameText
        val rButton = binding.registerButton

        viewModel.setupTextWatchers(email,
            password,
            fullName,
            button =  rButton,
            context = requireContext(),
            signIn = false)
        viewModel.setupObservers(email,
            password,
            fullName,
            context = requireContext(),
            viewLifecycleOwner = viewLifecycleOwner)

        /*viewModel.registrationStatus.observe(viewLifecycleOwner) { task ->
            task?.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
                    Toast.makeText(context, "Registered in", Toast.LENGTH_SHORT).show()

                } else {
                    authResultTask.exception?.let {
                        Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
                    }
                }
            }*/
        
            binding.registerButton.setOnClickListener {
                if (binding.policyCheckbox.isChecked) {
                    val emailText = binding.emailRegText.text.toString()
                    val passwordText = binding.passwordText.text.toString()
                    val fullNameText = binding.fullNameText.text.toString()
                    var bundle = Bundle()

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
                        if (binding.vetCheckbox.isChecked) {
                            val vetUser = User(
                                userEmail = emailText,
                                userPassword = passwordText,
                                userName = fullNameText
                            )
                            val bundle = Bundle().apply {
                                putParcelable("vetUser", vetUser)
                            }
                            Log.d(TAG, "Vet User comes from register: ${vetUser.userId}, ${vetUser.userEmail}, ${vetUser.userName}, ${vetUser.userPassword}, ${vetUser.isVet}")
                            findNavController().navigate(R.id.action_registerFragment_to_vetRegisterFragment, bundle)
                        } else {
                            viewModel.register(email = binding.emailRegText.text.toString(),
                                password = binding.passwordText.text.toString(),
                                fullName = fullNameText,
                                isVet = false)
                        }

                    }


                } else {
                    viewModel.errorLiveData.postValue("Please read and agree to Terms and Policy before sign up.")
                }

            }

            viewModel.errorLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        viewModel.handleGoogleSignInResult(data, requestCode, signIn = false)
    }


        private fun setupFacebookSignIn() {

            LoginManager.getInstance().registerCallback(callbackManager, object :
                FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    viewModel.handleFacebookAccessToken(result.accessToken, signIn = false)
                }

                override fun onCancel() {
                    // Handle cancellation
                }

                override fun onError(error: FacebookException) {
                    // Handle error
                }
            })
        }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
