package com.example.petcareproject.viewmodels

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.animation.AnimationUtils
import android.widget.Button
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
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import org.json.JSONException
import java.text.SimpleDateFormat
import java.util.Locale

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {
    private val TAG = "VetAuth"
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

    val showResetPasswordDialog = MutableLiveData<Boolean>()

    private val firebaseAuth = FirebaseAuth.getInstance()
/*
    private lateinit var googleSignInClient: GoogleSignInClient
*/

    val userLiveData = MutableLiveData<FirebaseUser?>()
    val errorLiveData = MutableLiveData<String>()

    private var isNewUser: Boolean? = false
    var authErrorCounter = 0



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
        checkUserExists(email) { exists ->
            println("'${email}':  ${exists}")
            if (exists) {
                repository.loginUser(email, password)
                    .addOnCompleteListener { authResultTask ->
                        if (authResultTask.isSuccessful) {
                            errorLiveData.postValue("Sign in successful.")
                            userLiveData.postValue(firebaseAuth.currentUser)
                            _loginStatus.postValue(authResultTask)
                        } else {
                            _loginStatus.postValue(authResultTask)
                                authErrorCounter++
                                if (authErrorCounter >= 2) {
                                    showResetPasswordDialog.postValue(true)
                                } else {
                                    errorLiveData.postValue("Wrong password. Please try again.")
                                }
                            }
                    }
            } else {
                errorLiveData.postValue("No user found associated to this account.")
            }

        }
    }

    fun register(email: String,
                 password: String,
                 fullName: String?,
                 isVet: Boolean = false,
                 registeredVetClinic: String? = null,
                 vetUri: Uri? = null) {
        checkUserExists(email) { exists ->
            Log.d(TAG, "${email}:  ${exists}")
            if (!exists) {
                repository.registerUser(email, password)
                    .addOnCompleteListener { authResultTask ->
                        if (authResultTask.isSuccessful) {
                            Log.d(TAG, "REGISTER: ${authResultTask.result?.user}")
                            saveUserToFirestore(fullName, isVet)
                            if (isVet) {
                                val vet = authResultTask.result?.user
                                if (vet != null) {
                                    Log.d(TAG, "VET: REGISTER USER ${vet}")
                                    Log.d(TAG, "VET: REGISTER UID ${vet.uid}")
                                    Log.d(TAG, "VET: REGISTER NAME ${vet.displayName}")
                                    saveVetToFirestore(fullName, registeredVetClinic)
                                }
                                uploadFileToFirebaseStorage(uri = vetUri!!, vet!!.uid)
                                errorLiveData.postValue("Sign up successful.")
                                userLiveData.postValue(firebaseAuth.currentUser)
                                _registrationStatus.postValue(authResultTask)

                            } else {
                                errorLiveData.postValue("Sign up successful.")
                                userLiveData.postValue(firebaseAuth.currentUser)
                                _registrationStatus.postValue(authResultTask)
                            }

                        } else {
                            println(authResultTask.exception)
                            errorLiveData.postValue(authResultTask.exception?.message)
                            _registrationStatus.postValue(authResultTask)
                        }
                    }
            } else {
                errorLiveData.postValue("The email address is already in use by another account.")
            }
        }
    }

    private fun uploadFileToFirebaseStorage(uri: Uri, vetId: String) {
        val storageRef = Firebase.storage.reference
        val fileName = "${vetId}_registrationConfirmationFile"
        val fileRef = storageRef.child("veterinarians/files/vetRegistrationConfirmation/$fileName")

        Log.d(TAG, "uploadFileToFirebaseStorage URI: ${uri}")
        Log.d(TAG, "uploadFileToFirebaseStorage vetId: ${vetId}")

        val uploadTask = fileRef.putFile(uri)
        uploadTask.addOnSuccessListener {
            Log.d(TAG,"File uploaded successfully.")
            // Handle success, if needed
        }.addOnFailureListener {
            Log.d(TAG,"Failed to upload file.")
            // Handle failure, if needed
        }
    }
    fun saveVetToFirestore(fullName: String? = null, registeredVetClinic: String? = null)  {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()

            // SimpleDateFormat to format timestamps in a readable format
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val vetMap = hashMapOf(
                "vetId" to user.uid,
                "vetName" to user.displayName,
                "vetClinicName" to registeredVetClinic,
                "createdAt" to dateFormat.format(user.metadata?.creationTimestamp ?: 0),
                "lastSignInAt" to dateFormat.format(user.metadata?.lastSignInTimestamp ?: 0),
                "isActive" to false
            )

            // Additional fields can be added as needed
            if (user.displayName != null) {
                vetMap["vetName"] = user.displayName  // Ensures the name is included if available
            } else if (fullName != null) {
                vetMap["vetName"] = fullName
            } else {
                vetMap["vetName"] = null
            }

            // Document reference based on user ID
            val vetDocRef = db.collection("veterinarians").document(user.uid)
           // updateUserToVet(user.uid)
            vetDocRef.set(vetMap)
                .addOnSuccessListener {
                    println("Vet data saved successfully")
                }
                .addOnFailureListener { e ->
                    println("Error saving user data: $e")
                }


        } else {
            println("No authenticated user found")
        }

    }
    /*fun updateUserToVet(userId: String) {
        val db = FirebaseFirestore.getInstance()
        val userDocument = db.collection("users").document(userId)

        // Set isVet to true
        userDocument.update("isVet", true)
            .addOnSuccessListener {
                // Handle success (e.g., display a message to the user)
                Log.d("Firestore", "Document successfully updated!")
            }
            .addOnFailureListener { e ->
                // Handle failure (e.g., display an error message)
                Log.w("Firestore", "Error updating document", e)
            }
    }*/
    fun logout(googleSignInClient: GoogleSignInClient) {
        repository.signOut()
        firebaseAuth.signOut()
        googleSignInClient.signOut()
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
                    if (!signIn) {
                        saveUserToFirestore()
                    }

                }

                override fun onCancel() {
                    errorLiveData.postValue("Facebook sign-in cancelled")
                }

                override fun onError(error: FacebookException) {
                    errorLiveData.postValue("Facebook sign-in error: ${error.message}")
                }
            })
    }


    fun proceedWithFirebaseSignIn(credential: AuthCredential, signIn: Boolean = true) {
    FirebaseAuth.getInstance().signInWithCredential(credential)
        .addOnCompleteListener { authResultTask ->
            if (authResultTask.isSuccessful) {
                if (signIn) {
                    _loginStatus.postValue(authResultTask)
                    userLiveData.postValue(firebaseAuth.currentUser)
                    errorLiveData.postValue("Sign in successful.")
                } else {
                    errorLiveData.postValue("Sign up successful.")
                    userLiveData.postValue(firebaseAuth.currentUser)
                    saveUserToFirestore()
                    _registrationStatus.postValue(authResultTask)
                }
            }
        }
}
    fun saveUserToFirestore(fullName: String? = null, isVet: Boolean = false) {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        if (user != null) {
            val db = FirebaseFirestore.getInstance()
            // SimpleDateFormat to format timestamps in a readable format
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

            val userMap = hashMapOf(
                "uid" to user.uid,
                "name" to user.displayName,
                "email" to user.email,
                "provider" to user.providerData.map { it.providerId }.joinToString(", "),
                "createdAt" to dateFormat.format(user.metadata?.creationTimestamp ?: 0),
                "lastSignInAt" to dateFormat.format(user.metadata?.lastSignInTimestamp ?: 0),
                "isVet" to isVet,
                "isActive" to true
            )

            // Additional fields can be added as needed
            if (user.displayName != null) {
                userMap["name"] = user.displayName  // Ensures the name is included if available
            } else if (fullName != null) {
                userMap["name"] = fullName
            } else {
                userMap["name"] = null
            }

            // Document reference based on user ID
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.set(userMap)
                .addOnSuccessListener {
                    println("User data saved successfully")
                }
                .addOnFailureListener { e ->
                    println("Error saving user data: $e")
                }
        } else {
            println("No authenticated user found")
        }
    }

    fun setupTextWatchers(
            email: EditText,
            password: EditText,
            fullName: EditText? = null,
            button: Button,
            context: Context,
            signIn: Boolean = true
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
                    println(s.toString())
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
                    if (!signIn) {
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
        fun setupGoogleSignIn(activity: Activity): GoogleSignInClient {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(activity.getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
            return GoogleSignIn.getClient(activity, gso)
        }


    fun firebaseAuthWithGoogle(idToken: String?, signIn: Boolean = true) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Sign-in success, update the UI with the signed-in user's information
                    if (signIn) {
                        _loginStatus.postValue(task)
                    } else {
                        saveUserToFirestore()
                    }

                } else {
                    // If sign-in fails, display a message to the user.
                    errorLiveData.postValue("Authentication failed: ${task.exception?.message}")
                }
            }
    }

        fun handleGoogleSignInResult(data: Intent?, requestCode: Int, signIn: Boolean = true) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            if (requestCode == 1001) {
                try {
                    val account = task.getResult(ApiException::class.java)
                    account?.email?.let { email ->
                        checkUserExists(email) { exists ->
                            println("${account.email}: ${signIn},  ${exists}")
                            if (exists) {
                                if (!signIn) {
                                    errorLiveData.postValue("Account already signed up. Please sign in instead.")
                                } else {
                                    firebaseAuthWithGoogle(account.idToken, signIn) //sign in
                                }
                            } else {
                                if (signIn) {
                                    errorLiveData.postValue("Please sign up before signing in.")
                                } else {
                                    firebaseAuthWithGoogle(account.idToken, signIn)
                                }
                            }
                        }
                    }
                } catch (e: ApiException) {
                    errorLiveData.postValue("Google sign-in failed: ${e.statusCode}")
                }
            }
        }


      fun handleFacebookAccessToken(token: AccessToken, signIn: Boolean = true) {
      val request = GraphRequest.newMeRequest(token) { jsonObject, response ->
          try {
              val email = jsonObject?.getString("email")?.trim()?.lowercase()
              if (email != null) {
                  checkUserExists(email) { exists ->
                      println("${email}: $exists")
                      if (exists) {
                          if (!signIn) {
                              errorLiveData.postValue("Account already signed up. Please sign in instead.")
                          } else {
                              val credential = FacebookAuthProvider.getCredential(token.token) //sign in
                              proceedWithFirebaseSignIn(credential, signIn)
                          }
                      } else {
                          if (signIn) {
                              errorLiveData.postValue("Please sign up before signing in.")
                          } else {
                              val credential = FacebookAuthProvider.getCredential(token.token)
                              proceedWithFirebaseSignIn(credential, signIn) //sign up
                          }
                      }
                  }
              } else {
                  errorLiveData.postValue("Email not found from Facebook.")
              }
          } catch (e: JSONException) {
              errorLiveData.postValue("Failed to parse email from Facebook data: ${e.message}")
          }
      }

      val parameters = Bundle()
      parameters.putString("fields", "id,name,email")
      request.parameters = parameters
      request.executeAsync()
      }



    fun checkUserExists(email: String, onComplete: (exists: Boolean) -> Unit) {
        val db = FirebaseFirestore.getInstance()
        val emailLowercase = email.trim().lowercase()
        try {
            db.collection("users").whereEqualTo("email", emailLowercase).get()
                .addOnSuccessListener { documents ->
                    val exists = !documents.isEmpty
                    println("Query for '$emailLowercase': Found = $exists")
                    onComplete(exists)
                }
                .addOnFailureListener { exception ->
                    println("Failed to check user '$emailLowercase': ${exception.message}")
                    errorLiveData.postValue("Failed to check user: ${exception.message}")
                    onComplete(false)  // Assume user does not exist if there's a failure
                }
        } catch (e: Exception) {
            println(e.message)
        }

    }




}

