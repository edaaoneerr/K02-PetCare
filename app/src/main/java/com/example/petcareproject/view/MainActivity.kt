package com.example.petcareproject.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.example.petcareproject.R
import com.example.petcareproject.util.PreferenceHelper
import com.example.petcareproject.view.authview.LoginFragment
import com.example.petcareproject.view.greetingview.OnboardingFragment
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlin.math.log


class MainActivity : AppCompatActivity() {
    private lateinit var preferenceHelper: PreferenceHelper
    val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)

        // Initialize SharedPreferences helper
        preferenceHelper = PreferenceHelper(this)
        println(preferenceHelper.isFirstLaunch)

        preferenceHelper = PreferenceHelper(this)


        if (preferenceHelper.isFirstLaunch) {
            navigateToOnboarding()
            preferenceHelper.isFirstLaunch = false
        } else {
            navigateBasedOnAuthentication()
        }

    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {

        return super.onCreateView(name, context, attrs)
    }

    private fun navigateToOnboarding() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navController.navigate(R.id.onboardingFragment)
    }

    private fun navigateBasedOnAuthentication() {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        if (FirebaseAuth.getInstance().currentUser != null) {
            navController.navigate(R.id.homeFragment)
        } else {
            navController.navigate(R.id.loginFragment)
        }
    }

}