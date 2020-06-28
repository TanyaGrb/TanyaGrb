package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity
data class VKSourceModel(
    @PrimaryKey
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

    companion object CREATOR : Parcelable.Creator<VKSourceModel> {
        override fun createFromParcel(parcel: Parcel): VKSourceModel {
            return VKSourceModel(parcel)
        }

        override fun newArray(size: Int): Array<VKSourceModel?> {
            return arrayOfNulls(size)
        }

        fun parseGroup(json: JSONObject) =
            VKSourceModel(json.getInt("id"), json.getString("name"), json.getString("photo_100"))

        fun parseProfile(json: JSONObject) = VKSourceModel(
            id = json.getInt("id"),
            name = "${json.getString("first_name")} ${json.getString("last_name")}",
            photo_100 = json.getString("photo_100")
        )
    }


}