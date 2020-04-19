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
)

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
    }
}

data class PhotoCharacteristics(val width: Int = 0, val height: Int = 0)