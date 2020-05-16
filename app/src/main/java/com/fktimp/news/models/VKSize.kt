package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VKAttachments::class,
            parentColumns = arrayOf("wallParentAttachments"),
            childColumns = arrayOf("wallParentSize"),
            onDelete = ForeignKey.CASCADE
        )]
)
data class VKSize(
    var type: String = "",
    var url: String = "",
    var width: Int = 0,
    var height: Int = 0,
    @PrimaryKey(autoGenerate = true)
    var sizeId: Int,
    var wallParentSize: String = ""

) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readInt(), 0
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
            json.getInt("height"), 0
        )

    }
}