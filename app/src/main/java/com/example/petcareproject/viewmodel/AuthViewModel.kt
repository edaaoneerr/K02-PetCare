package com.example.petcareproject.viewmodel

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.example.petcareproject.R
import com.example.petcareproject.repository.AuthRepository
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
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import org.json.JSONException

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginStatus = MutableLiveData<Task<AuthResult>?>()
    val loginStatus: LiveData<Task<AuthResult>?> = _loginStatus
    private val _registrationStatus = MutableLiveData<Task<AuthResult>?>()
    val registrationStatus: MutableLiveData<Task<AuthResult>?> = _registrationStatus
    private val _cursorDrawable = MutableLiveData<Int>()
    val cursorDrawable: LiveData<Int> = _cursorDrawable

    private val _emailValid = MutableLiveData<Boolean>()
    val emailValid: LiveData<Boolean> = _emailValid

    private val _passwordValid = MutableLiveData<Boolean>()
    val passwordValid: LiveData<Boolean> = _passwordValid

    private val _emailEmpty =  MutableLiveData<Boolean>(true)
    val emailEmpty: LiveData<Boolean> = _emailEmpty

    private val _passwordEmpty = MutableLiveData<Boolean>(true)
    val passwordEmpty: LiveData<Boolean> = _passwordEmpty

    private val _fullNameEmpty = MutableLiveData<Boolean>(true)
    val fullNameEmpty: LiveData<Boolean> = _fullNameEmpty

    private val _fullNameShouldShake = MutableLiveData<Boolean>()
    val fullNameShouldShake: LiveData<Boolean> = _fullNameShouldShake

    private val _emailShouldShake = MutableLiveData<Boolean>()
    val emailShouldShake: LiveData<Boolean> = _emailShouldShake

    private val _passwordShouldShake = MutableLiveData<Boolean>()
    val passwordShouldShake: LiveData<Boolean> = _passwordShouldShake

    private val firebaseAuth = FirebaseAuth.getInstance()
    private lateinit var googleSignInClient: GoogleSignInClient

    val userLiveData = MutableLiveData<FirebaseUser?>()
    val errorLiveData = MutableLiveData<String>()

    private var isNewUser: Boolean? = false



    fun validateEmail(email: String): Boolean {
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _emailValid.postValue(isValid)  // Always post value, regardless of the result
        _emailShouldShake.postValue(!isValid)
        _cursorDrawable.value = if (!isValid) R.drawable.red_cursor else R.drawable.purple_cursor
        return isValid
    }

    fun validatePassword(password: String): Boolean {
        val isValid = password.length >= 6
        _passwordValid.value = isValid
        _passwordShouldShake.value =  !isValid
        _cursorDrawable.value = if (!isValid) R.drawable.red_cursor else R.drawable.purple_cursor
        return isValid
    }


    fun checkEmailEmpty(email: String): Boolean {
        _emailEmpty.value = email.isEmpty()
        _emailShouldShake.value = email.isEmpty()
        _cursorDrawable.value = if (email.isEmpty()) R.drawable.red_cursor else R.drawable.purple_cursor
        return email.isEmpty()

    }
    fun checkPasswordEmpty(password: String): Boolean {
        _passwordEmpty.value = password.isEmpty()
        _passwordShouldShake.value = password.isEmpty()
        _cursorDrawable.value = if (password.isEmpty()) R.drawable.red_cursor else R.drawable.purple_cursor
        return password.isEmpty()
    }
    fun checkFullNameEmpty(fullName: String): Boolean {
        _fullNameEmpty.value = fullName.isEmpty()
        _fullNameShouldShake.value = fullName.isEmpty()
        _cursorDrawable.value = if (fullName.isEmpty()) R.drawable.red_cursor else R.drawable.purple_cursor
        return fullName.isEmpty()
    }

    fun login(email: String, password: String) {
         repository.loginUser(email, password).addOnCompleteListener { authResultTask ->
            if (authResultTask.isSuccessful) {
                errorLiveData.postValue("Sign in successful.")
                userLiveData.postValue(firebaseAuth.currentUser)
                _loginStatus.postValue(authResultTask)
            } else {
                errorLiveData.postValue("${authResultTask.exception?.message}")
            }
        }
    }


    fun register(email: String, password: String) {
        repository.registerUser(email, password).addOnCompleteListener { authResultTask ->
            if (authResultTask.isSuccessful) {
                        errorLiveData.postValue("Sign up successful.")
                        userLiveData.postValue(firebaseAuth.currentUser)
                        _registrationStatus.postValue(authResultTask)
                } else {
                errorLiveData.postValue("Account already exists. Please sign in instead.")
            }
            _registrationStatus.postValue(authResultTask)
        }
    }
    fun logout() {
        repository.signOut()
        firebaseAuth.signOut()
        // Update any LiveData or state as needed
        _loginStatus.postValue(null)
        userLiveData.postValue(null)
    }

    // Setup Facebook SignIn
    fun setupFacebookSignIn(callbackManager: CallbackManager, signIn: Boolean = true) {
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(result: LoginResult) {
                    handleFacebookAccessToken(result.accessToken, signIn)
                }

                override fun onCancel() {
                    errorLiveData.postValue("Facebook sign-in cancelled")
                }

                override fun onError(error: FacebookException) {
                    errorLiveData.postValue("Facebook sign-in error: ${error.message}")
                }
            })
    }


    fun handleFacebookAccessToken(token: AccessToken, signIn: Boolean = true) {
        val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
            try {
                val email = jsonObject?.getString("email")
                val credential = FacebookAuthProvider.getCredential(token.token)
                if (email != null) {
                    proceedWithFirebaseSignIn(credential, signIn)
                }
            } catch (e: JSONException) {
                errorLiveData.postValue("Failed to parse email from Facebook data.")
            }
        }

        val parameters = Bundle()
        parameters.putString("fields", "id,name,email")
        request.parameters = parameters
        request.executeAsync()
    }

    fun checkIfNewUser(credential: AuthCredential): Boolean? {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { authResultTask ->
                isNewUser = authResultTask.result.additionalUserInfo?.isNewUser
                println("YENİ KULLANICI" + isNewUser)
            }
        return isNewUser;
    }
    fun proceedWithFirebaseSignIn(credential: AuthCredential, signIn: Boolean = true) {
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { authResultTask ->
                isNewUser = authResultTask.result.additionalUserInfo?.isNewUser
                println("YENİ KULLANICI" + isNewUser)
                if (signIn) {
                    if (isNewUser == true) {
                        errorLiveData.postValue("No account found. Please sign up before signing in.")
                        return@addOnCompleteListener
                    } else {
                        if (authResultTask.isSuccessful) {
                            _loginStatus.postValue(authResultTask)
                            userLiveData.postValue(firebaseAuth.currentUser)
                            errorLiveData.postValue("Sign in successful.")
                        } else {
                            errorLiveData.postValue("Authentication failed: ${authResultTask.exception?.message}")
                        }
                    }
                } else {
                    if (isNewUser == true) {
                        if (authResultTask.isSuccessful) {
                            errorLiveData.postValue("Sign up successful.")
                            userLiveData.postValue(firebaseAuth.currentUser)
                            _registrationStatus.postValue(authResultTask)
                        } else {
                            errorLiveData.postValue("Authentication failed: ${authResultTask.exception?.message}")
                        }
                    } else {
                        errorLiveData.postValue("Account already exists. Please sign in instead.")
                        return@addOnCompleteListener
                    }
                }
            }
    }
        fun setupTextWatchers(
            email: EditText,
            password: EditText,
            fullName: EditText? = null,
            button: Button,
            context: Context
        ) {

            email.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkEmailEmpty(s.toString())
                    println(s.toString())

                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            password.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    println(s.toString())
                    checkPasswordEmpty(s.toString())
                    password.setTextCursorDrawable(cursorDrawable.value!!)
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            fullName?.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    checkFullNameEmpty(s.toString())
                }

                override fun afterTextChanged(s: Editable?) {
                }
            })

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    if (fullName != null) {
                        updateButtonState(button, context, signIn = false);
                    } else {
                        updateButtonState(button, context);
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            }

            email.addTextChangedListener(textWatcher)
            password.addTextChangedListener(textWatcher)
            fullName?.addTextChangedListener(textWatcher)

        }

        private fun updateButtonState(button: Button, context: Context, signIn: Boolean = true) {
            try {
                if (signIn) {
                    button.isEnabled = !emailEmpty.value!! && !passwordEmpty.value!!
                } else {
                    button.isEnabled =
                        !emailEmpty.value!! && !passwordEmpty.value!! && !fullNameEmpty.value!!
                }
                if (button.isEnabled) button.setBackgroundColor(
                    ContextCompat.getColor(context, R.color.pet)
                );
                else button.setBackgroundColor(ContextCompat.getColor(context, R.color.grey));
            } catch (e: Exception) {
                println(e)
            }
        }

        fun observeShake(
            shouldShake: LiveData<Boolean>,
            editText: EditText,
            context: Context,
            viewLifecycleOwner: LifecycleOwner
        ) {

            shouldShake.observe(viewLifecycleOwner, Observer { shouldShake ->
                if (shouldShake) editText.startAnimation(
                    AnimationUtils.loadAnimation(
                        context,
                        R.anim.shake
                    )
                )
            })
        }

        fun setupObservers(
            email: EditText,
            password: EditText,
            fullName: EditText? = null,
            context: Context,
            viewLifecycleOwner: LifecycleOwner
        ) {
            // Observing the shake animation triggers
            observeShake(emailShouldShake, email, context, viewLifecycleOwner)
            observeShake(passwordShouldShake, password, context, viewLifecycleOwner)
            fullName?.let {
                observeShake(fullNameShouldShake, it, context, viewLifecycleOwner)
            }
        }

        fun isPolicyChecked(policyCheckbox: CheckBox): Boolean {
            if (!policyCheckbox.isChecked) {
                errorLiveData.postValue("Please read and agree to the Terms & Policy before sign up.")
                return false
            } else {
                return true
            }
        }

        fun validateAuthentication(
            authType: String,
            email: String? = null,
            password: String? = null,
            signIn: Boolean = true,
            policyCheckbox: CheckBox? = null,
            vetCheckBox: CheckBox? = null,
            context: Context? = null,
            activity: Activity? = null,
            callbackManager: CallbackManager? = null,
            navigateToVetReg: (() -> Unit)? = null,
            navigateToHome: (() -> Unit)? = null
        ) {
            if (!signIn && !(isPolicyChecked(policyCheckbox!!))) {
                return
            }
            if (!signIn && vetCheckBox!!.isChecked) {
                navigateToVetReg?.invoke()
            }
            when (authType) {
                "Firebase" -> {
                    if (!isAuthenticationValid(email!!, password!!)) {
                        return
                    } else {
                        handleFirebaseAuthentication(
                            signIn,
                            email,
                            password
                        ) { navigateToHome?.invoke() }
                    }
                }

                "Google" -> handleGoogleAuthentication(activity!!)
                "Facebook" -> handleFacebookAuthentication(activity!!, callbackManager)
            }
        }

        private fun isAuthenticationValid(email: String, password: String): Boolean {
            var isValid = true
            if (!validateEmail(email)) {
                errorLiveData.postValue("Please enter a valid email address.")
                isValid = false
            }
            if (!validatePassword(password)) {
                errorLiveData.postValue("Password can't be be 6 or less characters")
                isValid = false
            }
            return isValid
        }

        private fun handleFirebaseAuthentication(
            signIn: Boolean? = true,
            email: String,
            password: String,
            navigateToHome: (() -> Unit)
        ) {
            if (signIn == true) {
                println("LOGIN")
                login(email, password)
            } else {
                register(email, password)
            }
        }

        fun setupGoogleSignIn(activity: Activity): GoogleSignInClient {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(activity, gso)
        }

        fun handleGoogleAuthentication(activity: Activity) {
            googleSignInClient.revokeAccess().addOnCompleteListener { revokeTask ->
                if (revokeTask.isSuccessful) {
                    val signInIntent = googleSignInClient.signInIntent
                    activity.startActivityForResult(
                        signInIntent,
                        1001
                    ) // Request code for Google SignIn
                } else {
                    errorLiveData.postValue("Failed to revoke previous sessions: ${revokeTask.exception?.message}")
                }
            }
        }

        // Handle Google SignIn result
        fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>, signIn: Boolean = true) {
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                proceedWithFirebaseSignIn(credential, signIn)


            } catch (e: ApiException) {
                errorLiveData.postValue(
                    "Google sign-in failed: ${
                        GoogleSignInStatusCodes.getStatusCodeString(
                            e.statusCode
                        )
                    }"
                )
            }
        }

        private fun handleFacebookAuthentication(
            activity: Activity?,
            callbackManager: CallbackManager?
        ) {
            setupFacebookSignIn(callbackManager!!)
            val permissions = listOf("email", "public_profile")
            LoginManager.getInstance().logInWithReadPermissions(activity!!, permissions)
        }


    }

