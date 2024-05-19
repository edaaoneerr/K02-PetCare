package com.example.petcareproject.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.petcareproject.R
import com.example.petcareproject.adapters.VetAdapter
import com.example.petcareproject.databinding.FragmentVetListBinding
import com.example.petcareproject.model.Vet
import com.example.petcareproject.viewmodels.VetListViewModel

class VetListFragment :  Fragment(), VetAdapter.OnItemClickListener {
    private var _binding: FragmentVetListBinding? = null
    private val binding get() = _binding!!
    private var vetsAdapter: VetAdapter? = null
    private val viewModel: VetListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVetListBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val vets = arguments?.getParcelableArrayList<Vet>("vets")
        // Populate UI with clinic details
        vets?.let {
            updateAdapter(vets)
        }

    }
    override fun onStart() {
        super.onStart()
        viewModel.setupVetRecyclerView()
    }
    private fun updateAdapter(vets: List<Vet>) {
        vetsAdapter = VetAdapter(vets, object : VetAdapter.OnItemClickListener {
            override fun onItemClick(vet: Vet?) {
                if (vet != null) {
                    val bundle = Bundle().apply {
                        putParcelable("vet", vet)
                    }
                    findNavController().navigate(R.id.action_vetListFragment_to_vetDetailFragment, bundle)
                }
            }

        })
        binding.vetClinicsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.vetClinicsRecyclerView.adapter = vetsAdapter
    }
    override fun onItemClick(vet: Vet?) {
        findNavController().navigate(R.id.action_vetListFragment_to_vetDetailFragment)
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}