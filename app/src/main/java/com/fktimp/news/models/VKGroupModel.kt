package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKGroupModel(
    val id: Int,
    val name: String,
    val photo_100: String,
    var isPicked: Boolean? = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        false
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeString(photo_100)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKGroupModel> {
        override fun createFromParcel(parcel: Parcel): VKGroupModel {
            return VKGroupModel(parcel)
        }

        override fun newArray(size: Int): Array<VKGroupModel?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) =
            VKGroupModel(json.getInt("id"), json.getString("name"), json.getString("photo_100"))
    }


}