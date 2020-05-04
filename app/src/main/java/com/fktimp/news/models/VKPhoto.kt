package com.fktimp.news.models

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject

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

data class PhotoCharacteristics(val width: Int = 0, val height: Int = 0)