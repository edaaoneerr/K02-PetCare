package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Pet(
    val petId: String? = null,
    val userId: String = "",
    var petName: String = "",
    var petType: String = "",
    var petBreed: String = "",
    var isMale: Boolean,
    var petWeight: String = "",
    var petBirthday: Timestamp?,

    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readBoolean(),
        parcel.readString()?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(petId)
        parcel.writeString(userId)
        parcel.writeString(petName)
        parcel.writeString(petType)
        parcel.writeString(petBreed)
        parcel.writeBoolean(isMale)
        parcel.writeString(petWeight)
        parcel.writeParcelable(petBirthday, flags)
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
