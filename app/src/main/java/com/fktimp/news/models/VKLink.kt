package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject


data class VKLink(
    val url: String = "",
    val title: String = "",
    val caption: String = "",
    val photo: VKPhoto = VKPhoto()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(VKPhoto::class.java.classLoader) ?: VKPhoto()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(title)
        parcel.writeString(caption)
        parcel.writeParcelable(photo, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKLink> {
        override fun createFromParcel(parcel: Parcel): VKLink {
            return VKLink(parcel)
        }

        override fun newArray(size: Int): Array<VKLink?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKLink = VKLink(
            json.getString("url"),
            json.optString("title", ""),
            json.optString("caption", ""),
            if (json.optJSONObject("photo") != null) VKPhoto.parse(json.getJSONObject("photo")) else VKPhoto()
        )
    }
}