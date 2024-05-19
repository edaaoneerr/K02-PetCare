package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable

data class User(
    val userId: String? = null,
    val userName: String = "",
    val userEmail: String = "",
    val userPassword: String,
    val provider: String = "",
    val userProfileImage: String = "",
    val createdAt: String = "",
    val lastSignInAt: String = "",
    val isVet: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString().toString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(userName)
        parcel.writeString(userEmail)
        parcel.writeString(userPassword)
        parcel.writeString(provider)
        parcel.writeString(userProfileImage)
        parcel.writeString(createdAt)
        parcel.writeString(lastSignInAt)
        parcel.writeByte(if (isVet) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User = User(parcel)

        override fun newArray(size: Int): Array<User?> = arrayOfNulls(size)
    }
}
