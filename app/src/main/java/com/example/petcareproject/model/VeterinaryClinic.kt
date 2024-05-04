package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable

data class VeterinaryClinic(
    val clinicId: String? = null,
    val name: String = "",
    val specialty: String = "",
    val rating: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    val address: String = "",
    var distance: Double = 0.0,
    var imageUrl: String = "",
): Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readDouble(),
        parcel.readString() ?: "",
        parcel.readDouble(),
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(clinicId)
        parcel.writeString(name)
        parcel.writeString(specialty)
        parcel.writeString(rating)
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(address)
        parcel.writeDouble(distance)
        parcel.writeString(imageUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VeterinaryClinic> {
        override fun createFromParcel(parcel: Parcel): VeterinaryClinic {
            return VeterinaryClinic(parcel)
        }

        override fun newArray(size: Int): Array<VeterinaryClinic?> {
            return arrayOfNulls(size)
        }
    }
}

