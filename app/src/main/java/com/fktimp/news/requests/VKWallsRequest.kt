package com.fktimp.news.requests


import com.fktimp.news.models.VKWall
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKWallsRequest(id: Int = 0) : VKRequest<List<VKWall>>("wall.get") {


    init {
        addParam("owner_id", id)
        addParam("count", 1)
    }


    override fun parse(r: JSONObject): List<VKWall> {
        val users = r.getJSONObject("response")
        return listOf(VKWall.parse(users))
    }
}