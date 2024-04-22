package com.example.petcareproject.view.authview

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
import com.example.petcareproject.viewmodel.AuthViewModel
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
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


        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is signed in, navigate directly to HomeFragment
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            // No user is signed in, set up sign-in options
            setupGoogleSignIn()
            setupFacebookSignIn()
        }

        binding.googleButtonLayout.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, 1001) // Request code for Google SignIn
        }

        binding.facebookLoginLayout.setOnClickListener {
            val permissions = listOf("email", "public_profile")
            LoginManager.getInstance().logInWithReadPermissions(this, permissions)

        }
        binding.loginButton.setOnClickListener {
            viewModel.login(binding.emailText.text.toString(), binding.passwordText.text.toString())
        }
        viewModel.loginStatus.observe(viewLifecycleOwner) { task ->
            task?.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    Toast.makeText(context, "Logged in", Toast.LENGTH_SHORT).show()

                } else {
                    authResultTask.exception?.let {
                    }
                }
            }
        }
        binding.createAccountText.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
    }

    private fun setupFacebookSignIn() {

        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                viewModel.loginWithFacebook(result.accessToken)

            }

            override fun onCancel() {
                // Handle cancellation
            }

            override fun onError(error: FacebookException) {
                // Handle error
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1001) { // Handle Google SignIn result
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnCompleteListener { handleSignInResult(it) }
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            viewModel.loginWithGoogle(account)
        } catch (e: ApiException) {
            // Log an error message or handle the user's failure to log in
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}