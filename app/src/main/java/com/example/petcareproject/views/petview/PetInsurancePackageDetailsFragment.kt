package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentPetInsurancePackageDetailsBinding
import com.example.petcareproject.model.InsurancePackage
import com.example.petcareproject.model.Step
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PetInsurancePackageDetailsFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentPetInsurancePackageDetailsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetInsurancePackageDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val insurancePackage = arguments?.getParcelable<InsurancePackage>("insurance_package")
        insurancePackage?.let {
            binding.packageName.text = it.insuranceName
            populatePackageDetails(it.insuranceDetails)
        }


        binding.nextButton.setOnClickListener {
            val steps = arrayListOf(
                Step(R.drawable.ic_check, "Take your pet to the vet", "Visit any licensed vet, emergency clinic or specialist in the U.S. There's no network of providers to worry about."),
                Step(R.drawable.ic_check, "Send us your claim", "Pay your pet's vet bill, and send us your claim along with vet records and invoice from the visit."),
                Step(R.drawable.ic_check, "Get money back quickly", "We will follow up with your vet for any missing info. Claims are typically processed in less than 2 weeks.")
            )

            val bundle = Bundle().apply {
                putParcelableArrayList("steps", steps)
                putParcelable("insurance_package", insurancePackage)

            }
            val detailsFragment = PetInsuranceHowItWorksFragment().apply {
                arguments = bundle
            }

            // Use FragmentTransaction to add the new fragment and dismiss the current one on commit
            val transaction = parentFragmentManager.beginTransaction()
            transaction.add(detailsFragment, "PetInsuranceHowItWorksFragment")

            // Commit the transaction and add a commit listener to dismiss the current fragment
            transaction.runOnCommit {
                dismiss()
            }
            transaction.commitAllowingStateLoss()
        }

    }

    private fun populatePackageDetails(details: List<String>?) {
        details?.forEach { detail ->
            val detailView = LayoutInflater.from(context).inflate(
                R.layout.item_insurance_detail, binding.packageDetailsContainer, false
            )
            detailView.findViewById<TextView>(R.id.detail_description).text = detail
            binding.packageDetailsContainer.addView(detailView)

            val dividerView = View(context).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 1
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.black))
            }
            binding.packageDetailsContainer.addView(dividerView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
