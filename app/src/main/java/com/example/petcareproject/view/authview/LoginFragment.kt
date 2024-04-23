package com.example.petcareproject.view.authview

import android.app.Activity
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
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONException

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
        setupFacebookSignIn()

       if (FirebaseAuth.getInstance().currentUser != null) {
            // User is signed in, navigate directly to HomeFragment
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            // No user is signed in, set up sign-in options

        }
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
      viewModel.setupTextWatchers(email, password, null, button =  loginButton, context = requireContext())
        viewModel.setupObservers(email,
            password,
            context = requireContext(),
            viewLifecycleOwner = viewLifecycleOwner)


           /* val emailText = binding.emailText.text.toString()
            val passwordText = binding.passwordText.text.toString()
            // No user is signed in, set up sign-in options
            binding.googleButtonLayout.setOnClickListener {
                // Ensure previous Google sign-ins are cleared before starting a new sign-in attempt.
                viewModel.validateAuthentication("Google",
                    activity = requireActivity(),
                    context = requireContext())
            }

            binding.facebookLoginLayout.setOnClickListener {
                viewModel.validateAuthentication("Facebook",
                    activity = requireActivity(),
                    callbackManager = callbackManager,
                    context = requireContext())

            }
            binding.loginButton.setOnClickListener {
                viewModel.validateAuthentication("Firebase",
                    binding.emailText.text.toString(),
                    binding.passwordText.text.toString(),
                    navigateToHome = { navigateToHome() })
            }

        }*/

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
        viewModel.loginStatus.observe(viewLifecycleOwner) { task ->
            task?.addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                } else {
                    authResultTask.exception?.let {
                    }
                }
            }
        }

    }
    fun navigateToHome() {
        findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.addOnCompleteListener {
                handleGoogleSignInResult(it)
            }
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        // Revoke access to ensure the account selection dialog shows every time.
        googleSignInClient.revokeAccess().addOnCompleteListener {
            // Optionally handle completion of revoking access, such as enabling the sign-in button.
        }
    }

    private fun setupFacebookSignIn() {

        LoginManager.getInstance().registerCallback(callbackManager, object :
            FacebookCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }

            override fun onCancel() {
                // Handle cancellation
            }

            override fun onError(error: FacebookException) {
                // Handle error
            }
        })
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
            try {
                val email = jsonObject?.getString("email")
                val credential = FacebookAuthProvider.getCredential(token.token)
                if (email != null) {
                    try {
                        checkIfUserExists(credential, email)
                    } catch (e: Exception) {
                        showMessage(e.message.toString())
                    }
                }
            } catch (e: JSONException) {
                showMessage("Failed to parse email from Facebook data.")
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email")
        request.parameters = parameters
        request.executeAsync()
    }

    fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            checkIfUserExists(credential, account.email)
        } catch (e: ApiException) {
            showMessage("Google sign-in failed: ${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)}")
        }
    }

    private fun checkIfUserExists(credential: AuthCredential, email: String?): Boolean {
        if (email.isNullOrEmpty()) {
            showMessage("Email address is missing. Cannot proceed with authentication.")
        }
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email!!)
            .addOnCompleteListener { task ->
                isNewUser = task.result?.signInMethods?.isEmpty() ?: true
                if (isNewUser) {
                    showMessage("No account found. Please sign up before signing in.")

                } else {
                    proceedWithFirebaseSignIn(credential)
                }
            }
        return isNewUser
    }


    private fun proceedWithFirebaseSignIn(credential: AuthCredential) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { authResultTask ->
                if (authResultTask.isSuccessful) {
                    showMessage("Authentication successful.")
                    // Navigate or perform actions for authenticated users
                } else {
                    showMessage("Authentication failed: ${authResultTask.exception?.message}")
                }
            }
    }



fun showMessage(message: String) {
    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
}




}