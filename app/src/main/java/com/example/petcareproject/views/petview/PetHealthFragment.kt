package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.R
import com.example.petcareproject.adapters.TreatmentOption
import com.example.petcareproject.adapters.TreatmentOptionsAdapter
import com.example.petcareproject.databinding.FragmentPetHealthBinding


class PetHealthFragment : Fragment() {

    private var _binding: FragmentPetHealthBinding? = null
    private val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentPetHealthBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val treatmentOptions = listOf(
            TreatmentOption(R.drawable.ic_insurance, "Insurance"),
            TreatmentOption(R.drawable.ic_vaccination, "Vaccines"),
            TreatmentOption(R.drawable.ic_anti_parasite, "Anti-parasitical treatments"),
            TreatmentOption(R.drawable.ic_med_intervention, "Medical interventions"),
            TreatmentOption(R.drawable.ic_other_treatments, "Other treatments")
        )

        val adapter = TreatmentOptionsAdapter(treatmentOptions) { selectedLabel ->
            handleNavigation(selectedLabel)
        }

        binding.treatmentOptionsRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.treatmentOptionsRecyclerView.adapter = adapter

    }
    private fun handleNavigation(label: String) {
        when (label) {
            "Insurance" -> findNavController().navigate(R.id.action_petHealthFragment_to_petInsuranceFragment)
            /*"Vaccines" -> findNavController().navigate(R.id.action_treatmentOptionsFragment_to_vaccinesFragment)
            else -> findNavController().navigate(R.id.action_treatmentOptionsFragment_to_otherFragment)*/
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}