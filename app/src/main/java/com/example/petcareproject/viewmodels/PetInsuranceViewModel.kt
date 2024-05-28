package com.example.petcareproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcareproject.model.InsurancePackage
import com.example.petcareproject.model.PetInsurance
import com.example.petcareproject.model.toMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PetInsuranceViewModel: ViewModel() {
    private val _petInsurancePackages = MutableLiveData<List<InsurancePackage>>()
    val petInsurancePackages: LiveData<List<InsurancePackage>> = _petInsurancePackages

    private val _petInsurances = MutableLiveData<List<PetInsurance>>()
    val petInsurances: LiveData<List<PetInsurance>> = _petInsurances

    fun fetchInsurancePackages() {
        val db = FirebaseFirestore.getInstance()
        db.collection("insurance_packages")
            .get()
            .addOnSuccessListener { result ->
                val petInsurancePackages = mutableListOf<InsurancePackage>()
                viewModelScope.launch {
                    result.forEach { document ->
                        val insurancePackageId = document.id
                        val insurancePackageName = document.getString("insuranceName") ?: ""
                        val insurancePackagePrice = document.getString("insurancePrice") ?: ""
                        val insurancePackageDetails = document.get("insuranceDetails") as? List<String> ?: listOf()

                        val petInsurancePackage = InsurancePackage(
                            insurancePackageId,
                            insurancePackageName,
                            insurancePackagePrice,
                            insurancePackageDetails,
                        )
                        petInsurancePackages.add(petInsurancePackage)
                    }
                    updatePetInsurancePackages(petInsurancePackages)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }



    fun fetchPetInsurances() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pet_insurances")
            .whereEqualTo("petUserId", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                val petInsurances = mutableListOf<PetInsurance>()
                viewModelScope.launch {
                    result.forEach { document ->
                        val insuranceId = document.id
                        val petUserId = document.getString("petUserId") ?: ""
                        val petId = document.getString("petId") ?: ""

                        val petInsurance = PetInsurance(
                            insuranceId,
                            petUserId,
                            petId,
                        )
                        petInsurances.add(petInsurance)
                    }
                    updatePetInsurances(petInsurances)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun fetchInsurances() {
        val db = FirebaseFirestore.getInstance()
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return

        viewModelScope.launch {
            try {
                // Fetch pet insurances for the current user
                val petInsurancesSnapshot = db.collection("pet_insurances")
                    .whereEqualTo("petUserId", userId)
                    .get()
                    .await()

                val petInsurances = petInsurancesSnapshot.map { document ->
                    PetInsurance(
                        petInsuranceId = document.getString("petInsuranceId") ?: "",
                        petUserId = document.getString("petUserId") ?: "",
                        petId = document.getString("petId") ?: ""
                    )
                }

                println(petInsurances)

                // Fetch insurance packages for each pet insurance
                val insurancePackages = petInsurances.mapNotNull { petInsurance ->
                    try {
                        val insurancePackageSnapshot = db.collection("insurance_packages")
                            .whereEqualTo("insuranceId", petInsurance.petInsuranceId)
                            .get()
                            .await()

                        insurancePackageSnapshot.documents.map { document ->
                            InsurancePackage(
                                insuranceId = document.id,
                                insuranceName = document.getString("insuranceName") ?: "",
                                insurancePrice = document.getString("insurancePrice") ?: "",
                                insuranceDetails = document.get("insuranceDetails") as? List<String> ?: listOf()
                            )
                        }

                    } catch (e: Exception) {
                        println("Error getting insurance packages: $e")
                        null
                    }
                }.flatten()

                println(insurancePackages)

                // Update LiveData
                updatePetInsurances(petInsurances.toMutableList())
                updatePetInsurancePackages(insurancePackages.toMutableList())

            } catch (e: Exception) {
                println("Error getting documents: $e")
            }
        }
    }

    fun saveInsurancePackagesToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val insurancePackages = listOf(
            InsurancePackage(
                "",
                insuranceName = "Basic Pack",
                insurancePrice = "$20/month",
                insuranceDetails = listOf(
                    "Accidents and Injuries - $10,000",
                    "Illnesses and Conditions - $10,000",
                    "Exams and Prescriptions - $5,000"
                )
            ),
            InsurancePackage(
                "",
                "Comfy Pack",
                "$25/month",
                listOf(
                    "Accidents and Injuries - $20,000",
                    "Illnesses and Conditions - $20,000",
                    "Exams and Prescriptions - $10,000",
                    "Procedures and Diagnostics - $10,000",
                    "Holistic/Alternative Therapies - $3,000",
                    "Other Emergencies - $3,000"
                )
            ),
            InsurancePackage(
                "",
                "King Pack",
                "$35/month",
                listOf(
                    "Accidents and Injuries - $30,000",
                    "Illnesses and Conditions - $30,000",
                    "Exams and Prescriptions - $15,000",
                    "Procedures and Diagnostics - $15,000",
                    "Holistic/Alternative Therapies - $5,000",
                    "Other Emergencies - $5,000"
                )
            )
        )

        insurancePackages.forEach { insurancePackage ->
            // Create a new document reference
            val documentReference = db.collection("insurance_packages").document()

            // Set the document ID as the insuranceId
            val insurancePackageWithId = insurancePackage.copy(insuranceId = documentReference.id)

            // Add the insurance package with the updated insuranceId
            documentReference
                .set(insurancePackageWithId.toMap())
                .addOnSuccessListener {
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                }
        }
    }

    fun saveInsurancePackage(insurancePackage: InsurancePackage) {
        val db = FirebaseFirestore.getInstance()
            val documentReference = db.collection("insurance_packages").document()
            // Set the document ID as the insuranceId
            val insurancePackageWithId = insurancePackage.copy(insuranceId = documentReference.id)
            println(insurancePackageWithId)
            // Add the insurance package with the updated insuranceId
            documentReference
                .set(insurancePackageWithId.toMap())
                .addOnSuccessListener {
                    println("DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->
                    println("Error adding document: $e")
                }
        }

    fun savePetInsurance(insurance: PetInsurance) {
        val db = FirebaseFirestore.getInstance()
        val documentReference = db.collection("pet_insurances").document()
        // Set the document ID as the insuranceId
        // Add the insurance package with the updated insuranceId
        documentReference
            .set(insurance.toMap())
            .addOnSuccessListener {
                updatePetInsurances(mutableListOf(insurance))
                println("Pet Insurance added with ID: ${insurance.petInsuranceId}")
                fetchInsurances()
            }
            .addOnFailureListener { e ->
                println("Error adding document: $e")
            }
    }

    fun checkIfPetInsuranceExists(userId: String, insuranceId: String, callback: (Boolean) -> Unit) {
        FirebaseFirestore.getInstance().collection("pet_insurances")
            .whereEqualTo("petUserId", userId)
            .whereEqualTo("petInsuranceId", insuranceId)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    callback(false) // Insurance does not exist
                } else {
                    callback(true) // Insurance exists
                }
            }
            .addOnFailureListener { exception ->
                println("Error checking if insurance exists" + exception.message)
                callback(false) // Consider insurance not existing on error
            }
    }

    fun updatePetInsurances(petInsuranceList: MutableList<PetInsurance>) {
        println("Updating Pet Insurances")
        println(petInsuranceList)
        _petInsurances.postValue(petInsuranceList)
        println(_petInsurances.value)

    }

    fun updatePetInsurancePackages(insurancePackageList: List<InsurancePackage>) {
        println("Updating Packages")
        _petInsurancePackages.postValue(insurancePackageList)
    }


}