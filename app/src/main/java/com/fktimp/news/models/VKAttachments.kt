package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.*
import org.json.JSONObject

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = VKWallPostModel::class,
            parentColumns = arrayOf("post_id"),
            childColumns = arrayOf("wallParentAttachments"),
            onDelete = ForeignKey.CASCADE
        )],
    indices = [Index(value = ["wallParentAttachments"], unique = true)]
)
data class VKAttachments(
    var type: String = "",
    @Ignore
    var photo: List<VKSize>? = null,
    @Ignore
    var link: VKLink? = null,
    @PrimaryKey(autoGenerate = true)
    var attachmentId: Int = 0,
    var wallParentAttachments: Int = 0
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.createTypedArrayList(VKSize.CREATOR),
        parcel.readParcelable(VKLink::class.java.classLoader)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeTypedList(photo)
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

        fun parse(json: JSONObject): VKAttachments {
            val photoField = json.optJSONObject("photo")
            val sizesJsonArray = photoField?.getJSONArray("sizes")
            val sizes = if (sizesJsonArray != null)
                listOf(
                    VKSize.parse(sizesJsonArray[0] as JSONObject),
                    VKSize.parse(sizesJsonArray[sizesJsonArray.length() - 1] as JSONObject)
                ) else null

            return VKAttachments(
                json.getString("type"),
                sizes,
                if (json.optJSONObject("link") != null) VKLink.parse(json.getJSONObject("link")) else null
            )
        }
    }
}