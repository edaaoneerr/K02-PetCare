package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable

data class InsurancePackage(
    val insuranceId: String = "",
    val insuranceName: String = "",
    val insurancePrice: String = "",
    val insuranceDetails: List<String> = listOf()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList() ?: listOf()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(insuranceId)
        parcel.writeString(insuranceName)
        parcel.writeString(insurancePrice)
        parcel.writeStringList(insuranceDetails)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<InsurancePackage> {
        override fun createFromParcel(parcel: Parcel): InsurancePackage {
            return InsurancePackage(parcel)
        }

        override fun newArray(size: Int): Array<InsurancePackage?> {
            return arrayOfNulls(size)
        }
    }
}
fun InsurancePackage.toMap(): Map<String, Any> {
    return mapOf(
        "insuranceId" to insuranceId,
        "insuranceName" to insuranceName,
        "insurancePrice" to insurancePrice,
        "insuranceDetails" to insuranceDetails,
    )
}