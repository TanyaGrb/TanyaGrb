package com.fktimp.news.models

import org.json.JSONObject


data class VKSearchModel(
    val items: ArrayList<VKWallPostModel> = ArrayList(),
    var sources: List<VKSourceModel>,
    val next_from: String,
    val total_count: Int
) {
    companion object CREATOR {
        fun parse(json: JSONObject): VKSearchModel {
            val itemsJson = json.getJSONArray("items")
            val items = List(itemsJson.length()) {
                VKWallPostModel.parseSearchWallPost(itemsJson[it] as JSONObject)
            } as ArrayList
            val sources: ArrayList<VKSourceModel> = ArrayList()
            val profiles = json.optJSONArray("profiles")
            val groups = json.optJSONArray("groups")

            for (index in 0 until (profiles?.length() ?: 0))
                sources.add(VKSourceModel.parseProfile(profiles!![index] as JSONObject))

            for (index in 0 until (groups?.length() ?: 0))
                sources.add(VKSourceModel.parseGroup(groups!![index] as JSONObject))
            return VKSearchModel(
                items = items,
                sources = sources,
                next_from = json.optString("next_from"),
                total_count = json.getInt("total_count")
            )
        }
    }
}