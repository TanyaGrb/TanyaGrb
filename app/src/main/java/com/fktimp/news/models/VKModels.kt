package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKWallPostModel(
    var source_id: Int = 0,
    var date: Long = 0,
    var text: String = "",
    var reply_post_id: Int = 0,
    var attachments: ArrayList<VKAttachments> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readLong(),
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.createTypedArrayList(VKAttachments.CREATOR) as ArrayList<VKAttachments>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(source_id)
        parcel.writeLong(date)
        parcel.writeString(text)
        parcel.writeInt(reply_post_id)
        parcel.writeTypedList(attachments)
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
                json.getInt("source_id"),
                json.getLong("date"),
                json.getString("text"),
                json.optInt("reply_post_id", 0),
                attachments
            )
        }
    }
}

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

data class VKNewsModel(
    val items: ArrayList<VKWallPostModel> = ArrayList(),
    val groups: ArrayList<VKGroupModel> = ArrayList(),
    val next_from: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(VKWallPostModel.CREATOR) as ArrayList<VKWallPostModel>,
        parcel.createTypedArrayList(VKGroupModel.CREATOR) as ArrayList<VKGroupModel>,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(items)
        parcel.writeTypedList(groups)
        parcel.writeString(next_from)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKNewsModel> {
        override fun createFromParcel(parcel: Parcel): VKNewsModel {
            return VKNewsModel(parcel)
        }

        override fun newArray(size: Int): Array<VKNewsModel?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKNewsModel = VKNewsModel(
            List(
                json.getJSONArray("items").length()
            ) { VKWallPostModel.parse(json.getJSONArray("items")[it] as JSONObject) } as ArrayList<VKWallPostModel>,
            List(
                json.getJSONArray("groups").length()
            ) { VKGroupModel.parse(json.getJSONArray("groups")[it] as JSONObject) } as ArrayList<VKGroupModel>,
            json.optString("next_from", "")
        )

    }
}

data class VKAttachments(
    val type: String = "",
    val photo: VKPhoto = VKPhoto(),
    val link: VKLink = VKLink()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readParcelable(VKPhoto::class.java.classLoader) ?: VKPhoto(),
        parcel.readParcelable(VKLink::class.java.classLoader) ?: VKLink()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(type)
        parcel.writeParcelable(photo, flags)
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

        fun parse(json: JSONObject): VKAttachments = VKAttachments(
            json.getString("type"),
            if (json.optJSONObject("photo") != null) VKPhoto.parse(json.getJSONObject("photo")) else VKPhoto(),
            if (json.optJSONObject("link") != null) VKLink.parse(json.getJSONObject("link")) else VKLink()
        )


    }
}

data class VKPhoto(
    var sizes: ArrayList<VKSize> = ArrayList()
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.createTypedArrayList(VKSize.CREATOR) as ArrayList<VKSize>)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(sizes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKPhoto> {
        override fun createFromParcel(parcel: Parcel): VKPhoto {
            return VKPhoto(parcel)
        }

        override fun newArray(size: Int): Array<VKPhoto?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject): VKPhoto = VKPhoto(
            List(
                json.getJSONArray("sizes").length()
            ) { VKSize.parse(json.getJSONArray("sizes")[it] as JSONObject) } as ArrayList)


    }
}

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

data class PhotoCharacteristics(val width: Int = 0, val height: Int = 0)