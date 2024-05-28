package com.example.petcareproject.views.vetview

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.R
import com.example.petcareproject.adapters.vetadapters.VeterinaryClinicAdapter
import com.example.petcareproject.databinding.FragmentVetClinicListBinding
import com.example.petcareproject.model.VeterinaryClinic
import com.example.petcareproject.viewmodels.VetClinicListViewModel


class VetClinicListFragment : Fragment(), VeterinaryClinicAdapter.OnItemClickListener {

    private var _binding: FragmentVetClinicListBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VetClinicListViewModel by viewModels()
    private var vetClinicsAdapter: VeterinaryClinicAdapter? = null
    var name: String = ""
    var rating: String = ""


    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val PICK_IMAGE_REQUEST = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetClinicListBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.clinics.observe(viewLifecycleOwner) { clinics ->
            // Update your UI here
            updateAdapter(clinics)
        }

    }
    override fun onStart() {
        super.onStart()
        viewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("Permission granted")
            viewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())

        }
    }

    private fun updateAdapter(clinics: List<VeterinaryClinic>) {
        vetClinicsAdapter = VeterinaryClinicAdapter(clinics, object : VeterinaryClinicAdapter.OnItemClickListener {
            override fun onItemClick(clinic: VeterinaryClinic?) {
                if (clinic != null) {
                    val bundle = Bundle().apply {
                        putParcelable("clinic", clinic)
                    }
                    findNavController().navigate(R.id.action_vetClinicListFragment_to_vetDetailFragment, bundle)
                }
            }

        })
        binding.vetClinicsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.vetClinicsRecyclerView.adapter = vetClinicsAdapter
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onItemClick(item: VeterinaryClinic?) {
        findNavController().navigate(R.id.action_vetClinicListFragment_to_vetDetailFragment)
    }

}