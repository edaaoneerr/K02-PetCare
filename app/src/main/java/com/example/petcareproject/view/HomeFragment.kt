package com.example.petcareproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentHomeBinding
import com.example.petcareproject.databinding.FragmentLoginBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodel.AuthViewModel
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.facebookLogoutButton.setOnClickListener {

            try {
                val user = FirebaseAuth.getInstance().currentUser;
                viewModel.logout()
                // Optionally, add any other actions you'd like to perform after logging out
                Toast.makeText(context, "Logged out from Facebook", Toast.LENGTH_SHORT).show()
                System.exit(0);
            }
            catch (e: Exception) {
                println(e)
            }



        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}