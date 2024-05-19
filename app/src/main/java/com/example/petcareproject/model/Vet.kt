package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable

data class Vet(
    val vetId: String? = null,
    val clinicId: String = "",
    var vetClinicName: String = "",
    var vetName: String = "",
    var vetSpecialty: String = "",
    var vetPatientCount: Int = 0,
    var vetExperienceYears: Double = 0.0,
    var vetRating: String = "0.0",
    var vetReviewCount: Int = 0,
    var vetDistance: String = "0.0",
    var vetImageUrl: String = "",
    var vetAbout: String = "",
    var vetWorkingTime: Map<String,String> = mapOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readDouble(),
        parcel.readString()?: "",
        parcel.readInt(),
        parcel.readString()?: "0.0",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readHashMap(String::class.java.classLoader) as Map<String,String>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(vetId)
        parcel.writeString(clinicId)
        parcel.writeString(vetClinicName)
        parcel.writeString(vetName)
        parcel.writeString(vetSpecialty)
        parcel.writeInt(vetPatientCount)
        parcel.writeDouble(vetExperienceYears)
        parcel.writeString(vetRating)
        parcel.writeInt(vetReviewCount)
        parcel.writeString(vetDistance)
        parcel.writeString(vetImageUrl)
        parcel.writeString(vetAbout)
        parcel.writeMap(vetWorkingTime)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Vet> {
        override fun createFromParcel(parcel: Parcel): Vet {
            return Vet(parcel)
        }

        override fun newArray(size: Int): Array<Vet?> {
            return arrayOfNulls(size)
        }
    }
}
