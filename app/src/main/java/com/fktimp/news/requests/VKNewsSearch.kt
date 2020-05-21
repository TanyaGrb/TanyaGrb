package com.fktimp.news.requests

import com.fktimp.news.models.VKSearchModel
import com.vk.api.sdk.requests.VKRequest
import org.json.JSONObject

class VKNewsSearch(q: String, count: Int, startFrom: String) :
    VKRequest<VKSearchModel>("newsfeed.search") {
    init {
        addParam("q", q)
        addParam("extended", 1)
        addParam("count", count)
        addParam("start_from", startFrom)
    }

    override fun parse(r: JSONObject): VKSearchModel =
        VKSearchModel.parse(r.getJSONObject("response"))
}