package com.example.petcareproject.views.authview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.petcareproject.databinding.FragmentVetRegisterBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodels.AuthViewModel

class VetRegisterFragment : Fragment() {
    private var _binding: FragmentVetRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}