package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import org.json.JSONObject

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VKAttachments::class,
            parentColumns = arrayOf("wallParentAttachments"),
            childColumns = arrayOf("wallParentLink"),
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["wallParentLink"], unique = true)]
)
data class VKLink(
    var url: String = "",
    var title: String = "",
    var caption: String = "",
    @Ignore
    var photo: List<VKSize>? = null,
    @PrimaryKey(autoGenerate = true)
    var linkId: Int = 0,
    var wallParentLink: String = ""
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createTypedArrayList(VKSize.CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(url)
        parcel.writeString(title)
        parcel.writeString(caption)
        parcel.writeTypedList(photo)
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

        fun parse(json: JSONObject): VKLink {
            val photoField = json.optJSONObject("photo")
            val sizesJsonArray = photoField?.getJSONArray("sizes")
            val sizes = if (sizesJsonArray != null)
                listOf(
                    VKSize.parse(sizesJsonArray[0] as JSONObject),
                    VKSize.parse(sizesJsonArray[sizesJsonArray.length() - 1] as JSONObject)
                ) else null
            
            return VKLink(
                json.getString("url"),
                json.optString("title", ""),
                json.optString("caption", ""),
                sizes
            )
        }
    }
}