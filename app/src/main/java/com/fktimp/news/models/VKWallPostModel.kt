package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    indices = [Index(value = ["post_id"], unique = true)]
)
data class VKWallPostModel(
    @PrimaryKey
    var post_id: Int = 0,
    var source_id: Int = 0,
    var date: Long = 0,
    var text: String = "",
    var reply_post_id: Int = 0,
    @Ignore
    var attachments: ArrayList<VKAttachments>? = null,
    var topic_id: Int = -1,
    @Ignore
    var isSaved: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createTypedArrayList(VKAttachments.CREATOR),
        parcel.readInt(),
        parcel.readBoolean()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(post_id)
        parcel.writeInt(source_id)
        parcel.writeLong(date)
        parcel.writeString(text)
        parcel.writeInt(reply_post_id)
        parcel.writeTypedList(attachments)
        parcel.writeInt(topic_id)
        parcel.writeBoolean(isSaved)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKWallPostModel> {
        override fun createFromParcel(parcel: Parcel): VKWallPostModel {
            return VKWallPostModel(parcel)
        }

        override fun newArray(size: Int): Array<VKWallPostModel?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKWallPostModel {
            val attachments = if (json.optJSONArray("attachments") != null) List(
                json.getJSONArray("attachments").length()
            ) {
                VKAttachments.parse(
                    json.getJSONArray(
                        "attachments"
                    )[it] as JSONObject
                )
            } as ArrayList<VKAttachments> else ArrayList()
            return VKWallPostModel(
                json.getInt("post_id"),
                json.getInt("source_id"),
                json.getLong("date"),
                json.getString("text"),
                json.optInt("reply_post_id", 0),
                attachments,
                json.optInt("topic_id", -1)
            )
        }
    }
}