package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.petcareproject.adapters.InsuranceAdapter
import com.example.petcareproject.databinding.FragmentPetInsuranceBinding
import com.example.petcareproject.model.InsurancePackage
import com.example.petcareproject.viewmodels.PetInsuranceViewModel

class PetInsuranceFragment : Fragment() {

    private var _binding: FragmentPetInsuranceBinding? = null
    private val binding get() = _binding!!
    private val viewModel: PetInsuranceViewModel by viewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var insuranceAdapter: InsuranceAdapter
    private val insurancePackages = mutableListOf<InsurancePackage>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPetInsuranceBinding.inflate(inflater, container, false)
        return binding.root


    }

    override fun onResume() {
        super.onResume()
        println("fetch onresume")
        viewModel.fetchInsurances()
        // Fetch insurance packages and update the adapter

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.petInsurancePackages.observe(viewLifecycleOwner, Observer {
            println("observe")
            println(it)
            it?.let { insurancePackages ->
                val recyclerView = binding.insuranceRecyclerView
                recyclerView.visibility = View.VISIBLE
                binding.noInsuranceLayout.visibility = View.GONE
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
                val insuranceAdapter = InsuranceAdapter(insurancePackages)
                recyclerView.adapter = insuranceAdapter
            }
        })
        binding.addInsuranceButton.setOnClickListener {
            PetInsurancePackagesFragment().show(childFragmentManager, "PetInsurancePackagesFragment")
        }
    }

    fun updateInsuranceList() {
        viewModel.fetchInsurances()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
