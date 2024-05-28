package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.petcareproject.R
import com.example.petcareproject.adapters.petadapters.PetViewPagerAdapter
import com.example.petcareproject.databinding.FragmentPetBinding
import com.example.petcareproject.viewmodels.PetViewModel


class PetFragment : Fragment() {
    private var _binding: FragmentPetBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fetchPets()
        setupViewPager()

        binding.healthCard.setOnClickListener {
            findNavController().navigate(R.id.action_petFragment_to_petHealthFragment)
        }
        binding.activitiesCard.setOnClickListener {
            findNavController().navigate(R.id.action_petFragment_to_petActivityFragment)
        }
        binding.nutritionCard.setOnClickListener {
            findNavController().navigate(R.id.action_petFragment_to_petNutritionFragment)
        }

    }
    private fun setupViewPager() {
        val viewPager = binding.viewPager
        val dotsIndicator = binding.dotsIndicator

        viewModel.pets.observe(viewLifecycleOwner, Observer { petProfiles ->
            val adapter = PetViewPagerAdapter(petProfiles)
            viewPager.adapter = adapter
            dotsIndicator.setViewPager2(viewPager)
        })
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}