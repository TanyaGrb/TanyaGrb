package com.fktimp.news.models


import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKWallPost(
    val id: Int = 0,
    val owner_id: Int = 0,
    val date: Int = 0,
    val text: String = "",
    val reply_post_id: Int = 0
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readInt(),
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(owner_id)
        parcel.writeInt(date)
        parcel.writeString(text)
        parcel.writeInt(reply_post_id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKWallPost> {
        override fun createFromParcel(parcel: Parcel): VKWallPost {
            return VKWallPost(parcel)
        }

        override fun newArray(size: Int): Array<VKWallPost?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = VKWallPost(
            id = json.optInt("id", 0),
            owner_id = json.optInt("owner_id", 0),
            date = json.optInt("date", 0),
            text = json.optString("text", ""),
            reply_post_id = json.optInt("reply_post_id", 0)
        )
    }
}