package com.fktimp.news.requests

import com.fktimp.news.models.VKExecuteWallModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKExecuteWall(code: String) : VKRequest<VKExecuteWallModel>("execute") {

    init {
        addParam("code", code)
    }

    override fun parse(r: JSONObject): VKExecuteWallModel =
        Gson().fromJson(
            r.getJSONObject("response").toString(),
            object : TypeToken<VKExecuteWallModel?>() {}.type
        )
}