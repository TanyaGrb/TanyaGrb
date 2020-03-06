package com.fktimp.news.requests


import com.fktimp.news.models.VKWall
import com.vk.api.sdk.VKApiManager
import com.vk.api.sdk.VKApiResponseParser
import com.vk.api.sdk.VKMethodCall
import com.vk.api.sdk.exceptions.VKApiIllegalResponseException
import com.vk.api.sdk.internal.ApiCommand
import org.json.JSONException
import org.json.JSONObject

class VKWallsCommand(private val uids: IntArray = intArrayOf()): ApiCommand<List<VKWall>>() {
    override fun onExecute(manager: VKApiManager): List<VKWall> {

        if (uids.isEmpty()) {
            // if no uids, send user's data
            val call = VKMethodCall.Builder()
                .method("users.get")
                .args("fields", "photo_200")
                .version(manager.config.version)
                .build()
            return manager.execute(call, ResponseApiParser())
        } else {
            val result = ArrayList<VKWall>()
            val chunks = uids.toList().chunked(CHUNK_LIMIT)
            for (chunk in chunks) {
                val call = VKMethodCall.Builder()
                    .method("users.get")
                    .args("user_ids", chunk.joinToString(","))
                    .args("fields", "photo_200")
                    .version(manager.config.version)
                    .build()
                result.addAll(manager.execute(call, ResponseApiParser()))
            }
            return result
        }
    }

    companion object {
        const val CHUNK_LIMIT = 900
    }

    private class ResponseApiParser : VKApiResponseParser<List<VKWall>> {
        override fun parse(response: String): List<VKWall> {
            try {
                val ja = JSONObject(response).getJSONArray("response")
                val r = ArrayList<VKWall>(ja.length())
                for (i in 0 until ja.length()) {
                    val user = VKWall.parse(ja.getJSONObject(i))
                    r.add(user)
                }
                return r
            } catch (ex: JSONException) {
                throw VKApiIllegalResponseException(ex)
            }
        }
    }
}