package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable

data class PetInsurance(
    val petInsuranceId: String = "",
    val petUserId: String = "",
    val petId: String = "",
    val insuranceName: String = "",
    val startDate: String = "",
    val endDate: String = "",
    val status: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(petInsuranceId)
        parcel.writeString(petUserId)
        parcel.writeString(petId)
        parcel.writeString(insuranceName)
        parcel.writeString(startDate)
        parcel.writeString(endDate)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PetInsurance> {
        override fun createFromParcel(parcel: Parcel): PetInsurance {
            return PetInsurance(parcel)
        }

        override fun newArray(size: Int): Array<PetInsurance?> {
            return arrayOfNulls(size)
        }
    }
}

fun PetInsurance.toMap(): Map<String, Any> {
    return mapOf(
        "petInsuranceId" to petInsuranceId,
        "petUserId" to petUserId,
        "petId" to petId,
    )
}
