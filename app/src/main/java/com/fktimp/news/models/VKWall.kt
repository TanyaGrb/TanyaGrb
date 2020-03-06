package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONObject

data class VKWall(
    val count: Int = 0,
    val items: ArrayList<VKWallPost> = ArrayList()
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readArrayList(VKWallPost.javaClass.classLoader) as ArrayList<VKWallPost>
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(count)
        parcel.writeArray(arrayOf(items))
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<VKWall> {
        override fun createFromParcel(parcel: Parcel): VKWall {
            return VKWall(parcel)
        }

        override fun newArray(size: Int): Array<VKWall?> {
            return arrayOfNulls(size)
        }

        fun parse(json: JSONObject) = VKWall(
            count = json.optInt("count", 0),
            items = test2(json.get("items").toString())
        )


        private fun test2(json: String): ArrayList<VKWallPost> =
            Gson().fromJson(json, object : TypeToken<List<VKWallPost?>?>() {}.type)

    }
}