package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKSize(
    val type: String = "",
    val url: String = "",
    val width: Int = 0,
    val height: Int = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeString(url)
        parcel.writeInt(width)
        parcel.writeInt(height)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKSize> {
        override fun createFromParcel(parcel: Parcel): VKSize {
            return VKSize(parcel)
        }

        override fun newArray(size: Int): Array<VKSize?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKSize = VKSize(
            json.getString("type"),
            json.getString("url"),
            json.getInt("width"),
            json.getInt("height")
        )

    }
}