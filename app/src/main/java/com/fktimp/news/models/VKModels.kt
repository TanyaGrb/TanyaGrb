package com.fktimp.news.models

data class VKWallPostModel(
    val source_id: Int = 0,
    val date: Long = 0,
    val text: String = "",
    val reply_post_id: Int = 0
)

data class VKGroupModel(
    val id: Int,
    val name: String,
    val photo_100: String
)

data class VKNewsModel(
    val items: ArrayList<VKWallPostModel> = ArrayList(),
    val groups: ArrayList<VKGroupModel> = ArrayList(),
    val next_from: String?
)