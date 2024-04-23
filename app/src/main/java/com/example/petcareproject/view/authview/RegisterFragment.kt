package com.example.petcareproject.view.authview

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentRegisterBinding
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

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var callbackManager: CallbackManager
    private var isNewUser: Boolean = false

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
        val email = binding.emailText
        val password = binding.passwordText
        val fullName = binding.fullNameText
        val emailText = binding.emailText.text.toString()
        val passwordText = binding.passwordText.text.toString()
        val fullNameText = binding.fullNameText.text.toString()
        val registerButton = binding.registerButton
        val policyCheckbox = binding.policyCheckbox
        val vetCheckBox = binding.vetCheckbox

        registerButton.isEnabled = false

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


        viewModel.setupTextWatchers(email, password, fullName, button =  registerButton, context = requireContext())
        viewModel.setupObservers(email,
            password, fullName,
            context = requireContext(),
            viewLifecycleOwner = viewLifecycleOwner)


    /*    googleSignInClient = viewModel.setupGoogleSignIn(requireActivity())
        viewModel.setupTextWatchers(email, password, fullName, registerButton, requireContext())
        viewModel.setupObservers(email,
            password,
            fullName,
            requireContext(),
            viewLifecycleOwner)

        if (FirebaseAuth.getInstance().currentUser != null) {
            // User is signed in, navigate directly to HomeFragment
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        } else {
            // No user is signed in, set up sign-in options
            binding.googleButtonLayout.setOnClickListener {
                // Ensure previous Google sign-ins are cleared before starting a new sign-in attempt.
                viewModel.validateAuthentication(
                    signIn = false,
                    policyCheckbox = policyCheckbox,
                    vetCheckBox = vetCheckBox,
                    navigateToVetReg = { navigateToVetReg() },
                    authType = "Google",
                    context = requireContext(),
                    activity = requireActivity()
                )
            }

            binding.facebookLoginLayout.setOnClickListener {
                viewModel.validateAuthentication(
                    signIn = false,
                    policyCheckbox = policyCheckbox,
                    vetCheckBox = vetCheckBox,
                    navigateToVetReg = { navigateToVetReg() },
                    authType = "Facebook",
                    activity = requireActivity(),
                    callbackManager = callbackManager,
                    navigateToHome = { navigateToHome() },
                )
            }
            binding.registerButton.setOnClickListener {
                viewModel.validateAuthentication(
                    signIn = false,
                    policyCheckbox = policyCheckbox,
                    vetCheckBox = vetCheckBox,
                    navigateToVetReg = { navigateToVetReg() },
                    authType = "Firebase",
                    email = emailText,
                    password = passwordText,
                    navigateToHome = { navigateToHome() },
                    )
            }
        }*/
        viewModel.errorLiveData.observe(viewLifecycleOwner) {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
            binding.registerButton.setOnClickListener {
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
                    viewModel.register(email = binding.emailText.text.toString(), password = binding.passwordText.text.toString())

                }
            }

            viewModel.errorLiveData.observe(viewLifecycleOwner) {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
            viewModel.registrationStatus.observe(viewLifecycleOwner) { task ->
                task?.addOnCompleteListener { authResultTask ->
                    if (authResultTask.isSuccessful) {
                        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
                        Toast.makeText(context, "Registered", Toast.LENGTH_SHORT).show()
                    } else {
                        authResultTask.exception?.let {
                            Toast.makeText(context, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }


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
                            viewModel.proceedWithFirebaseSignIn(credential, signIn = false)
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
                viewModel.proceedWithFirebaseSignIn(credential, signIn = false)
            } catch (e: ApiException) {
                showMessage("Google sign-in failed: ${GoogleSignInStatusCodes.getStatusCodeString(e.statusCode)}")
            }
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


        fun navigateToHome() {
        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
    }
    fun navigateToVetReg() {
        findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
    }

    private fun setupTextWatchers(email: EditText, password:EditText) {
        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.checkEmailEmpty(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

       password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.checkPasswordEmpty(s.toString())
                binding.passwordText.setTextCursorDrawable(viewModel.cursorDrawable.value!!)
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateButtonState()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        email.addTextChangedListener(textWatcher)
        password.addTextChangedListener(textWatcher)

    }
    private fun updateButtonState() {
        val emailEmpty = viewModel.emailEmpty.value ?: true
        val passwordEmpty = viewModel.passwordEmpty.value ?: true
        binding.registerButton.isEnabled = !emailEmpty && !passwordEmpty
        if (binding.registerButton.isEnabled) binding.registerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pet));
        else binding.registerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey));
    }

    private fun setupObservers(email: EditText, password: EditText, fullName: EditText) {
        viewModel.observeShake(viewModel.emailShouldShake, email, requireContext(), viewLifecycleOwner)
        viewModel.observeShake(viewModel.passwordShouldShake, password, requireContext(), viewLifecycleOwner)
        viewModel.observeShake(viewModel.fullNameShouldShake, fullName, requireContext(), viewLifecycleOwner)

        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { result ->
            if (result != null) {
                println("Success")
                findNavController().navigate(R.id.action_registerFragment_to_homeFragment)
            } else {
                println("Couldnt register")
            }
        })
    }

    fun observeShake(shouldShake: LiveData<Boolean>, editText: EditText) {
        shouldShake.observe(viewLifecycleOwner, Observer { shouldShake ->
            if (shouldShake) editText.startAnimation(
                AnimationUtils.loadAnimation(
                    context,
                    R.anim.shake
                )
            )
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
