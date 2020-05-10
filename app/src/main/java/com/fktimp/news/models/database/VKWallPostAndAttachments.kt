package com.fktimp.news.models.database

import androidx.room.Embedded
import androidx.room.Relation
import com.fktimp.news.models.VKAttachments
import com.fktimp.news.models.VKLink
import com.fktimp.news.models.VKSize
import com.fktimp.news.models.VKWallPostModel

data class VKWallPostAndAttachments(
    @Embedded
    val postModel: VKWallPostModel,
    @Relation(
        parentColumn = "post_id",
        entity = VKAttachments::class,
        entityColumn = "wallParentAttachments"
    )
    val attachments: List<PhotoAndLinkAttachment>?
) {
    fun toVKWallPostModel(): VKWallPostModel {
        if (attachments == null) return postModel
        postModel.attachments = ArrayList()
        attachments.forEach {
            if (it.vkAttachments.type == "photo")
                it.vkAttachments.photo = it.photo
            else if (it.vkAttachments.type == "link") {
                it.vkAttachments.link = it.link
                it.vkAttachments.link!!.photo = it.photo
            }
            postModel.attachments!!.add(it.vkAttachments)
        }
        return postModel
    }
}

data class PhotoAndLinkAttachment(
    @Embedded
    val vkAttachments: VKAttachments,
    @Relation(
        parentColumn = "wallParentAttachments",
        entity = VKSize::class,
        entityColumn = "wallParentSize"
    )
    val photo: List<VKSize>?,
    @Relation(
        parentColumn = "wallParentAttachments",
        entity = VKLink::class,
        entityColumn = "wallParentLink"
    )
    val link: VKLink?
)