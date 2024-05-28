package com.example.petcareproject.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.petcareproject.model.Pet
import com.example.petcareproject.model.PetInsurance
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.util.Date

class PetViewModel: ViewModel() {
    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    fun fetchPets() {
        val db = FirebaseFirestore.getInstance()
        db.collection("pets")
            .whereEqualTo("userId", FirebaseAuth.getInstance().currentUser!!.uid)
            .get()
            .addOnSuccessListener { result ->
                val pets = mutableListOf<Pet>()
                viewModelScope.launch {
                    result.forEach { document ->
                        val petId = document.id
                        val userId = document.getString("userId") ?: ""
                        val petName = document.getString("petName") ?: ""
                        val petType = document.getString("petType")?: ""
                        val petBreed = document.getString("petBreed")?: ""
                        val isMale = document.getBoolean("isMale")
                        val petWeight = document.getString("petWeight")?: ""
                        val petBirthday = document.getTimestamp("petBirthday")

                        val pet = Pet(
                            petId,
                            userId,
                            petName,
                            petType,
                            petBreed,
                            isMale!!,
                            petWeight,
                            petBirthday
                        )
                        pets.add(pet)
                    }
                    updatePets(pets)
                }
            }
            .addOnFailureListener { exception ->
                println("Error getting documents: $exception")
            }
    }

    fun addExamplePetsToFirestore() {
        val db = FirebaseFirestore.getInstance()
        val pets = listOf(
            mapOf(
                "userId" to FirebaseAuth.getInstance().currentUser!!.uid,
                "petName" to "Buddy",
                "petType" to "Dog",
                "petBreed" to "Golden Retriever",
                "isMale" to true,
                "petWeight" to "30kg",
                "petBirthday" to Timestamp(Date(2020 - 1900, 5, 1)), // Birthday: June 1, 2020
                "createdAt" to Timestamp.now(),
                "isActive" to true
            ),
            mapOf(
                "userId" to FirebaseAuth.getInstance().currentUser!!.uid,
                "petName" to "Whiskers",
                "petType" to "Cat",
                "petBreed" to "Siamese",
                "isMale" to false,
                "petWeight" to "4kg",
                "petBirthday" to Timestamp(Date(2018 - 1900, 3, 14)), // Birthday: April 14, 2018
                "createdAt" to Timestamp.now(),
                "isActive" to true
            ),
            mapOf(
                "userId" to FirebaseAuth.getInstance().currentUser!!.uid,
                "petName" to "Max",
                "petType" to "Dog",
                "petBreed" to "Beagle",
                "isMale" to true,
                "petWeight" to "10kg",
                "petBirthday" to Timestamp(Date(2019 - 1900, 10, 22)), // Birthday: November 22, 2019
                "createdAt" to Timestamp.now(),
                "isActive" to true
            ),
            mapOf(
                "userId" to FirebaseAuth.getInstance().currentUser!!.uid,
                "petName" to "Shadow",
                "petType" to "Cat",
                "petBreed" to "Persian",
                "isMale" to true,
                "petWeight" to "5kg",
                "petBirthday" to Timestamp(Date(2017 - 1900, 7, 7)), // Birthday: August 7, 2017
                "createdAt" to Timestamp.now(),
                "isActive" to true
            )
        )

        pets.forEach { pet ->
            val docRef = db.collection("pets").document() // Create a document reference with auto-generated ID
            val petWithId = pet.toMutableMap().apply { put("petId", docRef.id) } // Add petId to the map

            docRef.set(petWithId)
                .addOnSuccessListener {
                    println("Pet added with ID: ${docRef.id}")
                }
                .addOnFailureListener { e ->
                    println("Error adding pet: $e")
                }
        }
    }




    fun updatePets(petList: List<Pet>) {
        _pets.postValue(petList)
    }
}