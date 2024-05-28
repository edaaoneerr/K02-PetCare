package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.adapters.InsurancePackageAdapter
import com.example.petcareproject.databinding.FragmentPetInsurancePackagesBinding
import com.example.petcareproject.model.InsurancePackage
import com.example.petcareproject.viewmodels.PetInsuranceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PetInsurancePackagesFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPetInsurancePackagesBinding? = null
    private val binding get() = _binding!!
    private var selectedPackage: InsurancePackage? = null
    private val viewModel: PetInsuranceViewModel by viewModels()
    private var adapter: InsurancePackageAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetInsurancePackagesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fetchInsurancePackages()

        viewModel.petInsurancePackages.observe(viewLifecycleOwner, Observer {
            adapter = InsurancePackageAdapter(it) { selectedPackage ->
                this.selectedPackage = selectedPackage
            }
            binding.packagesRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.packagesRecyclerView.adapter = adapter
        })



        binding.nextButton.setOnClickListener {
            selectedPackage?.let { insurancePackage ->
                val bundle = Bundle().apply {
                    putParcelable("insurance_package", insurancePackage)
                }
                val detailsFragment = PetInsurancePackageDetailsFragment().apply {
                    arguments = bundle
                }

                // Use FragmentTransaction to replace the current fragment with the new one
                val transaction = parentFragmentManager.beginTransaction()
                transaction.add(detailsFragment, "PetInsurancePackageDetailsFragment")

                // Commit the transaction and add a commit listener to dismiss the current fragment
                transaction.runOnCommit {
                    dismiss()
                }
                transaction.commitAllowingStateLoss()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
