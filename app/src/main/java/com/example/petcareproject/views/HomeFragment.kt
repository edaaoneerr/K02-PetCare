package com.example.petcareproject.views

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
import com.example.petcareproject.adapters.CampaignCarouselPagerAdapter
import com.example.petcareproject.adapters.PopularVeterinaryClinicAdapter
import com.example.petcareproject.adapters.ServiceCategoryAdapter
import com.example.petcareproject.databinding.FragmentHomeBinding
import com.example.petcareproject.model.ServiceCategory
import com.example.petcareproject.model.VeterinaryClinic
import com.example.petcareproject.viewmodels.VetClinicListViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: VetClinicListViewModel by viewModels()
    private var serviceCategoryAdapter: ServiceCategoryAdapter? = null
    private var popularVetClinicsAdapter: PopularVeterinaryClinicAdapter? = null
    var name: String = ""
    var imageUrl: String = ""
    var rating: String = ""
    var bundle = Bundle()
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Removing tint from icons and background from the BottomNavigationView
        binding.bottomNavigation.itemIconTintList = null
        binding.bottomNavigation.itemBackground = null

        binding.seeAllDoctors.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_vetClinicListFragment)
        }
        val bottomNav = binding.bottomNavigation
        bottomNav.setOnItemSelectedListener {
            println(it.title)
            when (it.title) {
                "Maps" -> {
                    true
                }
               "Pets" -> {
                   findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                "Bookings" -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                "Profile" -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }

                else -> {
                   false
                }
            }
        }
        viewModel.clinics.observe(viewLifecycleOwner) { clinics ->
            // Update your UI here
            updateVeterinaryClinicAdapter(clinics)
        }
        viewModel.serviceCategories.observe(viewLifecycleOwner) {serviceCategories ->
            println(serviceCategories)
            updateServiceCategoryAdapter(serviceCategories)
        }

        val viewPager = binding.campaignCarousel
        val items = listOf(R.drawable.campaign_1, R.drawable.campaign_2, R.drawable.campaign_3, R.drawable.campaign_4) // Replace with your image resources
        val adapter = CampaignCarouselPagerAdapter(items, requireContext())
        viewPager.adapter = adapter

        binding.messageIcon.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_chatFragment)
        }

    }

    override fun onStart() {
        super.onStart()
        viewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())
        viewModel.fetchServiceCategories()

    }
    override fun onStop() {
        super.onStop()
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            println("Permission granted")
            viewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())

        }
    }


    private fun updateVeterinaryClinicAdapter(clinics: List<VeterinaryClinic>) {
        popularVetClinicsAdapter = PopularVeterinaryClinicAdapter(clinics)

        bundle = Bundle().apply {
            putParcelableArrayList("clinics", ArrayList(clinics))
        }

        binding.popularVetClinicsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularVetClinicsRecyclerView.adapter = popularVetClinicsAdapter
    }

    private fun updateServiceCategoryAdapter(serviceCategories: List<ServiceCategory>) {
        serviceCategoryAdapter = ServiceCategoryAdapter(serviceCategories)
        binding.serviceCategoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.serviceCategoryRecyclerView.adapter = serviceCategoryAdapter
    }





}