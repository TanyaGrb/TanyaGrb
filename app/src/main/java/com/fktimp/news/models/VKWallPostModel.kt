package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import org.json.JSONObject

@Entity(
    indices = [Index(value = ["vkWallPostId"], unique = true)]
)
data class VKWallPostModel(
    var post_id: Int = 0,
    var source_id: Int = 0,
    var date: Long = 0,
    var text: String = "",
    @Ignore
    var attachments: ArrayList<VKAttachments>? = null,
    var topic_id: Int = -1,
    @Ignore
    var isSaved: Boolean = false,
    @PrimaryKey
    var vkWallPostId: String = "${source_id}_$date",
    @Ignore
    var copy_history: ArrayList<VKWallPostModel>? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        post_id = parcel.readInt(),
        source_id = parcel.readInt(),
        date = parcel.readLong(),
        text = parcel.readString() ?: "",
        attachments = parcel.createTypedArrayList(VKAttachments.CREATOR),
        topic_id = parcel.readInt(),
        vkWallPostId = parcel.readString() ?: "",
        copy_history = parcel.createTypedArrayList(CREATOR)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        with(parcel) {
            writeInt(post_id)
            writeInt(source_id)
            writeLong(date)
            writeString(text)
            writeTypedList(attachments)
            writeInt(topic_id)
            writeString(vkWallPostId)
            writeTypedList(copy_history)
        }
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
            val copyHistoryJson = json.optJSONArray("copy_history")
            val copyHistory = if (copyHistoryJson != null) List(copyHistoryJson.length()) {
                parseCopyHistory(copyHistoryJson[it] as JSONObject)
            } else null

            return VKWallPostModel(
                post_id = json.getInt("post_id"),
                source_id = json.getInt("source_id"),
                date = json.getLong("date"),
                text = json.getString("text"),
                attachments = getAttachments(json),
                topic_id = json.optInt("topic_id", -1),
                copy_history = copyHistory as ArrayList<VKWallPostModel>?
            )
        }

        private fun parseCopyHistory(json: JSONObject) = VKWallPostModel(
            post_id = json.getInt("id"),
            source_id = json.getInt("owner_id"),
            date = json.getLong("date"),
            text = json.getString("text"),
            attachments = getAttachments(json)
        )

        fun parseSearchWallPost(json: JSONObject): VKWallPostModel {
            // TODO репосты и упрощённый вид parseCopyHistory???
            return VKWallPostModel(
                post_id = json.getInt("id"),
                source_id = json.getInt("owner_id"),
                date = json.getLong("date"),
                text = json.getString("text"),
                attachments = getAttachments(json)
            )
        }

        private fun getAttachments(json: JSONObject): ArrayList<VKAttachments>? {
            val attachmentsJson = json.optJSONArray("attachments")
            return (if (attachmentsJson != null) List(attachmentsJson.length()) {
                VKAttachments.parse(attachmentsJson[it] as JSONObject)
            } else null) as ArrayList<VKAttachments>?
        }
    }
}