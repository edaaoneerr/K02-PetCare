package com.example.petcareproject.views.petview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.petcareproject.R
import com.example.petcareproject.databinding.FragmentPetInsuranceHowItWorksBinding
import com.example.petcareproject.model.InsurancePackage
import com.example.petcareproject.model.PetInsurance
import com.example.petcareproject.model.Step
import com.example.petcareproject.viewmodels.PetInsuranceViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth


class PetInsuranceHowItWorksFragment : BottomSheetDialogFragment() {
    private val viewModel: PetInsuranceViewModel by viewModels()

    private var _binding: FragmentPetInsuranceHowItWorksBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPetInsuranceHowItWorksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val steps = arguments?.getParcelableArrayList<Step>("steps")
        val insurancePackage = arguments?.getParcelable<InsurancePackage>("insurance_package")

        steps?.let {
            populateSteps(it)
        }

        binding.confirmButton.setOnClickListener {
            insurancePackage?.let { insurance ->
                println("Insurance package")
                println(insurancePackage)
                val insurancePet = PetInsurance(
                    petInsuranceId = insurance.insuranceId,
                    petUserId = FirebaseAuth.getInstance().currentUser!!.uid,
                    petId = ""
                )

                viewModel.checkIfPetInsuranceExists(insurancePet.petUserId, insurancePet.petInsuranceId) {exists ->
                    if (!exists) {
                        viewModel.savePetInsurance(insurancePet)

                        // Create a new fragment transaction to update the parent fragment
                        val transaction = parentFragmentManager.beginTransaction()

                        // Notify the parent fragment to update the insurance list
                        val parentFragment = parentFragmentManager.findFragmentByTag("PetInsuranceFragment") as? PetInsuranceFragment
                        parentFragment?.let {
                            transaction.runOnCommit {
                                it.updateInsuranceList()
                                dismiss() // Dismiss the current fragment after the update
                            }
                            transaction.commitAllowingStateLoss()
                        }
                    } else {
                        Toast.makeText(requireContext(), "You already have this insurance!", Toast.LENGTH_SHORT).show()
                    }
                }


            }
        }


    }

    private fun populateSteps(steps: List<Step>) {
        steps.forEach { step ->
            val stepView = LayoutInflater.from(context).inflate(
                R.layout.item_how_it_works_step, binding.stepsContainer, false
            )
            stepView.findViewById<ImageView>(R.id.step_icon).setImageResource(step.iconResId)
            stepView.findViewById<TextView>(R.id.step_title).text = step.title
            stepView.findViewById<TextView>(R.id.step_description).text = step.description
            binding.stepsContainer.addView(stepView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
