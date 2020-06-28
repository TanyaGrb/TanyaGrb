package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

data class VKNewsModel(
    val items: ArrayList<VKWallPostModel> = ArrayList(),
    val sources: ArrayList<VKSourceModel> = ArrayList(),
    val next_from: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createTypedArrayList(VKWallPostModel.CREATOR) as ArrayList<VKWallPostModel>,
        parcel.createTypedArrayList(VKSourceModel.CREATOR) as ArrayList<VKSourceModel>,
        parcel.readString() ?: ""
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeTypedList(items)
        parcel.writeTypedList(sources)
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

        fun parse(json: JSONObject): VKNewsModel {
            val sources: ArrayList<VKSourceModel> = ArrayList()
            val profiles = json.optJSONArray("profiles")
            val groups = json.optJSONArray("groups")

            for (index in 0 until (profiles?.length() ?: 0))
                sources.add(VKSourceModel.parseProfile(profiles!![index] as JSONObject))
            for (index in 0 until (groups?.length() ?: 0))
                sources.add(VKSourceModel.parseGroup(groups!![index] as JSONObject))
            val itemsJson = json.getJSONArray("items")
            return VKNewsModel(
                items = List(itemsJson.length())
                { VKWallPostModel.parse(itemsJson[it] as JSONObject) } as ArrayList<VKWallPostModel>,
                sources = sources,
                next_from = json.optString("next_from")
            )
        }
    }
}