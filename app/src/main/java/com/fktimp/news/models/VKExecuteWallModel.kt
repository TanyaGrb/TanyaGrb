package com.fktimp.news.models

data class VKExecuteWallModel(
    val wall: ArrayList<VKWallPost> = ArrayList(),
    val extra: Int = 0
)

data class VKWallPost(
    val id: Int = 0,
    val owner_id: Int = 0,
    val date: Long = 0,
    val text: String = "",
    val reply_post_id: Int = 0
)


data class VKWall(
    val count: Int = 0,
    val items: ArrayList<VKWallPost> = ArrayList()
)