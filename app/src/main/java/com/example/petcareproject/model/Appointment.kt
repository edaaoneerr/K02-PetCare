package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Appointment(
    val appointmentId: String? = null,
    val vetId: String = "",
    val userId: String = "",
    var appointmentDate: Timestamp? = null,
    var hasReview: Boolean,
    var isRescheduled: Boolean,
    var isActive: Boolean,

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader),
        parcel.readBoolean(),
        parcel.readBoolean(),
        parcel.readBoolean(),

    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(appointmentId)
        parcel.writeString(vetId)
        parcel.writeString(userId)
        parcel.writeParcelable(appointmentDate, flags)
        parcel.writeBoolean(hasReview)
        parcel.writeBoolean(isRescheduled)
        parcel.writeBoolean(isActive)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Appointment> {
        override fun createFromParcel(parcel: Parcel): Appointment {
            return Appointment(parcel)
        }

        override fun newArray(size: Int): Array<Appointment?> {
            return arrayOfNulls(size)
        }
    }
}
