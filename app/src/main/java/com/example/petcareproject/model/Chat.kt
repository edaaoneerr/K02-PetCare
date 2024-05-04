package com.example.petcareproject.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Chat(
    val chatId: String? = null,
    val chatParticipants: List<String>,
    val lastMessageId: String? = null,
    val lastMessage: String,
    val lastMessageSenderId: String,
    val lastMessageDate: Timestamp
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        mutableListOf<String>().apply {
            parcel.readStringList(this)
        },
        parcel.readString(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(chatId)
        parcel.writeStringList(chatParticipants)
        parcel.writeString(lastMessageId)
        parcel.writeString(lastMessage)
        parcel.writeString(lastMessageSenderId)
        parcel.writeParcelable(lastMessageDate, flags)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Chat> {
        override fun createFromParcel(parcel: Parcel): Chat = Chat(parcel)

        override fun newArray(size: Int): Array<Chat?> = arrayOfNulls(size)
    }
}

/*
class ChatFragment : Fragment() {
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}*/
