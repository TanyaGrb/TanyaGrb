package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

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