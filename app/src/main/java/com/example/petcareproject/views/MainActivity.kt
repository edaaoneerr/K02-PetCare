package com.example.petcareproject.views

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import com.example.petcareproject.R
import com.example.petcareproject.util.PreferenceHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging


class MainActivity : AppCompatActivity() {
    companion object {
        const val CHANNEL_ID = "fcm_default_channel"
    }
    private lateinit var preferenceHelper: PreferenceHelper
    val fragmentManager = supportFragmentManager
    val fragmentTransaction = fragmentManager.beginTransaction()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FirebaseApp.initializeApp(this)
        createNotificationChannel()


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
    private fun createNotificationChannel() {
        val channelID = "Default Channel"  // Make sure this is the same ID used everywhere
        val channelName = "Human Readable Name"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val notificationChannel = NotificationChannel(channelID, channelName, importance)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)


        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                println("FCM" + "Fetching FCM registration token failed" + task.exception)
                return@addOnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            println("FCM" + "FCM Token: $token")

    }
    }
    /*private fun sendRegistrationToServer(token: String) {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://yourapi.domain.com/") // Replace with your server URL
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)
        val tokenModel = TokenModel(token)

        service.registerToken(tokenModel).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    Log.d("FCM", "Token successfully registered to server")
                } else {
                    Log.e("FCM", "Server error while registering token")
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("FCM", "Failed to register token", t)
            }
        })
    }*/


}