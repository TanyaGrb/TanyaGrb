package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKAttachments(
    val type: String = "",
    val photo: VKPhoto = VKPhoto(),
    val link: VKLink = VKLink()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(VKPhoto::class.java.classLoader) ?: VKPhoto(),
        parcel.readParcelable(VKLink::class.java.classLoader) ?: VKLink()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeParcelable(photo, flags)
        parcel.writeParcelable(link, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKAttachments> {
        override fun createFromParcel(parcel: Parcel): VKAttachments {
            return VKAttachments(parcel)
        }

        override fun newArray(size: Int): Array<VKAttachments?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKAttachments = VKAttachments(
            json.getString("type"),
            if (json.optJSONObject("photo") != null) VKPhoto.parse(json.getJSONObject("photo")) else VKPhoto(),
            if (json.optJSONObject("link") != null) VKLink.parse(json.getJSONObject("link")) else VKLink()
        )


    }
}