package com.example.petcareproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.petcareproject.R
import com.example.petcareproject.repository.AuthRepository
import com.facebook.AccessToken
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider

class AuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _loginStatus = MutableLiveData<Task<AuthResult>?>()
    val loginStatus: LiveData<Task<AuthResult>?> = _loginStatus
    private val _registrationStatus = MutableLiveData<Task<AuthResult>>()
    val registrationStatus: LiveData<Task<AuthResult>> = _registrationStatus
    private val _cursorDrawable = MutableLiveData<Int>()
    val cursorDrawable: LiveData<Int> = _cursorDrawable

    private val _emailValid = MutableLiveData<Boolean>()
    val emailValid: LiveData<Boolean> = _emailValid

    private val _passwordValid = MutableLiveData<Boolean>()
    val passwordValid: LiveData<Boolean> = _passwordValid

    private val _emailEmpty = MutableLiveData<Boolean>()
    val emailEmpty: LiveData<Boolean> = _emailEmpty

    private val _passwordEmpty = MutableLiveData<Boolean>()
    val passwordEmpty: LiveData<Boolean> = _passwordEmpty

    private val _emailShouldShake = MutableLiveData<Boolean>()
    val emailShouldShake: LiveData<Boolean> = _emailShouldShake

    private val _passwordShouldShake = MutableLiveData<Boolean>()
    val passwordShouldShake: LiveData<Boolean> = _passwordShouldShake

    fun validateEmail(email: String): Boolean {
        val isValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        _emailValid.value = isValid
        _emailShouldShake.value = !isValid
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


    fun login(email: String, password: String) {
        val task = repository.loginUser(email, password)
        _loginStatus.postValue(task)
    }

    fun loginWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        repository.firebaseAuthWithCredential(credential).addOnCompleteListener {
            _loginStatus.postValue(it)
        }
    }

    fun loginWithFacebook(token: AccessToken) {
        val credential = FacebookAuthProvider.getCredential(token.token)
        repository.firebaseAuthWithCredential(credential).addOnCompleteListener {
            _loginStatus.postValue(it)
        }
    }

    fun register(email: String, password: String) {
        val task = repository.registerUser(email, password)
        _registrationStatus.postValue(task)
    }
    fun logout() {
        repository.signOut()
        // Update any LiveData or state as needed
        _loginStatus.postValue(null) // Assuming you have a LiveData for managing login state
    }

}
