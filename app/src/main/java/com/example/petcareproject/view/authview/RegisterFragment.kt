package com.example.petcareproject.view.authview

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
import androidx.lifecycle.Observer
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentRegisterBinding
import com.example.petcareproject.factory.AuthViewModelFactory
import com.example.petcareproject.repository.AuthRepository
import com.example.petcareproject.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository())
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val email= binding.emailText
        val password = binding.passwordText
        binding.registerButton.isEnabled = false


        setupTextWatchers(email, password)
        setupObservers(email, password)

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
                viewModel.register(emailText, passwordText)
            }
        }

    }

    private fun setupTextWatchers(email: EditText, password:EditText) {
        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("Email")
                viewModel.checkEmailEmpty(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {
            }
        })

       password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                println("Password")
                viewModel.checkPasswordEmpty(s.toString())
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

        binding.emailText.addTextChangedListener(textWatcher)
        binding.passwordText.addTextChangedListener(textWatcher)

    }
    private fun updateButtonState() {
        val emailEmpty = viewModel.emailEmpty.value ?: true
        val passwordEmpty = viewModel.passwordEmpty.value ?: true
        binding.registerButton.isEnabled = !emailEmpty && !passwordEmpty
        if (binding.registerButton.isEnabled) binding.registerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.pet));
        else binding.registerButton.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.grey));
    }

    private fun setupObservers(email: EditText, password: EditText) {

        viewModel.emailShouldShake.observe(viewLifecycleOwner, Observer { shouldShake ->
            println("Email should shake: " + shouldShake)
            if (shouldShake) email.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
        })

        viewModel.passwordShouldShake.observe(viewLifecycleOwner, Observer { shouldShake ->
            println("Password should shake: " + shouldShake)
            if (shouldShake) password.startAnimation(AnimationUtils.loadAnimation(context, R.anim.shake))
        })


        viewModel.registrationStatus.observe(viewLifecycleOwner, Observer { result ->
            if (result != null) {
                println("Success")
            } else {
                println("Couldnt register")
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
