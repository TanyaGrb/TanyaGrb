package com.fktimp.news.requests

import com.fktimp.news.models.VKGroupModel
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKGroupsById(group_ids: String) : VKRequest<List<VKGroupModel>>("groups.getById") {
    init {
        addParam("group_ids", group_ids)
    }

    override fun parse(r: JSONObject): List<VKGroupModel> {
        val items = r.getJSONArray("response")
        return List(items.length()) {
            VKGroupModel.parse(items.getJSONObject(it))
        }
    }
}