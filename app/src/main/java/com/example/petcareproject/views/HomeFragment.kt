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
import com.example.petcareproject.adapters.PopularVetAdapter
import com.example.petcareproject.adapters.PopularVeterinaryClinicAdapter
import com.example.petcareproject.adapters.ServiceCategoryAdapter
import com.example.petcareproject.databinding.FragmentHomeBinding
import com.example.petcareproject.model.ServiceCategory
import com.example.petcareproject.model.Vet
import com.example.petcareproject.model.VeterinaryClinic
import com.example.petcareproject.viewmodels.VetClinicListViewModel
import com.example.petcareproject.viewmodels.VetListViewModel


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val vetViewModel: VetListViewModel by viewModels()
    private val clinicViewModel: VetClinicListViewModel by viewModels()
    private var serviceCategoryAdapter: ServiceCategoryAdapter? = null
    private var popularVetClinicsAdapter: PopularVeterinaryClinicAdapter? = null
    private var popularVetsAdapter: PopularVetAdapter? = null
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

       /* binding.seeAllDoctors.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_vetListFragment)
        }*/
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
                    findNavController().navigate(R.id.action_homeFragment_to_bookingsFragment)
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
     /*   clinicViewModel.clinics.observe(viewLifecycleOwner) { clinics ->
            // Update your UI here
            println("HOMECLINIC")
            println(clinics)
            updatePopularVeterinaryClinicAdapter(clinics)
        }*/
        vetViewModel.vets.observe(viewLifecycleOwner) { vets ->
            // Update your UI here
            println("HOMEVET")
            println(vets)
            updatePopularVetAdapter(vets)

            binding.seeAllDoctors.setOnClickListener {
                val vetsArrayList = ArrayList(vets) // Convert your list to ArrayList
                val bundle = Bundle().apply {
                    putParcelableArrayList("vets", vetsArrayList)
                }
                findNavController().navigate(R.id.action_homeFragment_to_vetListFragment, bundle)
            }

        }
        clinicViewModel.serviceCategories.observe(viewLifecycleOwner) {serviceCategories ->
            println("HOMESERVC")
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
       // clinicViewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())
        clinicViewModel.fetchServiceCategories()
        vetViewModel.fetchLocationAndSetupVetRecyclerView(requireActivity(), requireContext())

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
            clinicViewModel.fetchLocationAndSetupClinicRecyclerView(requireActivity(), requireContext())

        }
    }


    private fun updatePopularVeterinaryClinicAdapter(clinics: List<VeterinaryClinic>) {
        popularVetClinicsAdapter = PopularVeterinaryClinicAdapter(clinics)

        bundle = Bundle().apply {
            putParcelableArrayList("clinics", ArrayList(clinics))
        }

        binding.popularVetClinicsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularVetClinicsRecyclerView.adapter = popularVetClinicsAdapter
    }
    private fun updatePopularVetAdapter(vets: List<Vet>) {
        popularVetsAdapter = PopularVetAdapter(vets, object : PopularVetAdapter.OnItemClickListener {
            override fun onItemClick(vet: Vet?) {
                if (vet != null) {
                    val bundle = Bundle().apply {
                        putParcelable("vet", vet)
                    }
                    findNavController().navigate(R.id.action_homeFragment_to_vetDetailFragment, bundle)
                }
            }

        })

        binding.popularVetsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.popularVetsRecyclerView.adapter = popularVetsAdapter
    }

    private fun updateServiceCategoryAdapter(serviceCategories: List<ServiceCategory>) {
        serviceCategoryAdapter = ServiceCategoryAdapter(serviceCategories)
        binding.serviceCategoryRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.serviceCategoryRecyclerView.adapter = serviceCategoryAdapter
    }





}