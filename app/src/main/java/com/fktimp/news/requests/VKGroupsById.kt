package com.fktimp.news.requests

import com.fktimp.news.models.VKSourceModel
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKGroupsById(group_ids: String) : VKRequest<List<VKSourceModel>>("groups.getById") {
    init {
        addParam("group_ids", group_ids)
    }

    override fun parse(r: JSONObject): List<VKSourceModel> {
        val items = r.getJSONArray("response")
        return List(items.length()) {
            VKSourceModel.parseGroup(items.getJSONObject(it))
        }
    }
}