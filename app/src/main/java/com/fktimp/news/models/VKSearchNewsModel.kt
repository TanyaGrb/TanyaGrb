package com.fktimp.news.models

import org.json.JSONObject


data class VKSearchModel(
    val items: ArrayList<VKSearchNewsModel> = ArrayList(),
    var profiles: List<VKProfileSearch>? = null,
    var groups: List<VKGroupsSearch>? = null,
    val next_from: String,
    val total_count: Int
) {
    companion object CREATOR {
        fun parse(json: JSONObject) = VKSearchModel(
            items = List(
                json.getJSONArray("items").length()
            ) { VKSearchNewsModel.parse(json.getJSONArray("items")[it] as JSONObject) } as ArrayList<VKSearchNewsModel>,
            profiles = if (json.optJSONArray("profiles") != null) List(
                json.getJSONArray("profiles").length()
            ) {
                VKProfileSearch.parse(json.getJSONArray("profiles")[it] as JSONObject)
            } else null,
            groups = if (json.optJSONArray("groups") != null) List(
                json.getJSONArray("groups").length()
            ) { VKGroupsSearch.parse(json.getJSONArray("groups")[it] as JSONObject) } else null,
            next_from = json.optString("next_from"),
            total_count = json.getInt("total_count")
        )
    }
}

data class VKSearchNewsModel(
    var id: Int,
    var owner_id: Int,
    var date: Long = 0,
    var text: String = "",
    var attachments: ArrayList<VKAttachments>? = null
) {

    companion object CREATOR {
        fun parse(json: JSONObject): VKSearchNewsModel {
            val attachments = if (json.optJSONArray("attachments") != null) List(
                json.getJSONArray("attachments").length()
            ) {
                VKAttachments.parse(
                    json.getJSONArray(
                        "attachments"
                    )[it] as JSONObject
                )
            } as ArrayList<VKAttachments> else null
            return VKSearchNewsModel(
                json.getInt("id"),
                json.getInt("owner_id"),
                json.getLong("date"),
                json.getString("text"),
                attachments
            )
        }

        fun toVKWallPostModel(vkSearchNewsModel: VKSearchNewsModel) = VKWallPostModel(
            post_id = vkSearchNewsModel.id,
            source_id = vkSearchNewsModel.owner_id,
            date = vkSearchNewsModel.date,
            text = vkSearchNewsModel.text,
            attachments = vkSearchNewsModel.attachments
        )
    }
}


data class VKProfileSearch(
    var id: Int,
    var first_name: String,
    var last_name: String,
    var photo_100: String,
    var screen_name: String
) {
    companion object CREATOR {
        fun parse(json: JSONObject) = VKProfileSearch(
            json.getInt("id"),
            json.getString("first_name"),
            json.getString("last_name"),
            json.getString("photo_100"),
            json.getString("screen_name")
        )

        fun toVKGroupModel(vkProfileSearch: VKProfileSearch) = VKGroupModel(
            id = vkProfileSearch.id,
            name = "${vkProfileSearch.first_name} ${vkProfileSearch.last_name}",
            photo_100 = vkProfileSearch.photo_100
        )
    }
}

data class VKGroupsSearch(
    var id: Int,
    var name: String,
    var photo_100: String,
    var screen_name: String
) {
    companion object CREATOR {
        fun parse(json: JSONObject) = VKGroupsSearch(
            json.getInt("id"),
            json.getString("name"),
            json.getString("photo_100"),
            json.getString("screen_name")
        )

        fun toVKGroupModel(vkGroupsSearch: VKGroupsSearch) = VKGroupModel(
            id = vkGroupsSearch.id,
            name = vkGroupsSearch.name,
            photo_100 = vkGroupsSearch.photo_100
        )
    }
}