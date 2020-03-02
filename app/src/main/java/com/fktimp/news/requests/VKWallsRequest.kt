package com.fktimp.news.requests


import com.fktimp.news.models.VKWall
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKWallsRequest(id: Int = 0, count: Int = 0) : VKRequest<VKWall>("wall.get") {

    init {
        addParam("owner_id", id)
        addParam("count", count)
    }

    override fun parse(r: JSONObject): VKWall =
            Gson().fromJson(
                r.getJSONObject("response").toString(),
                object : TypeToken<VKWall?>() {}.type
        )
}