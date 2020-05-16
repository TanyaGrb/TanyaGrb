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
        parentColumn = "vkWallPostId",
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
                for ((first, second) in it.photo?.splitByPair()!!) {
                    postModel.attachments!!.add(
                        it.vkAttachments.copy(photo = listOf(first as VKSize, second as VKSize))
                    )
                }
            else if (it.vkAttachments.type == "link") {
                it.vkAttachments.link = it.link
                it.vkAttachments.link!!.photo = it.photo
                postModel.attachments!!.add(it.vkAttachments)
            }
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

fun List<Any>.splitByPair() = this.slice(0..lastIndex step 2).zip(this.slice(1..lastIndex step 2))