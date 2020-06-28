package com.fktimp.news.models.database

import androidx.room.*
import com.fktimp.news.models.*
import kotlin.math.abs

@Dao
interface VKDao {

    @Transaction
    @Query("select * from vkwallpostmodel")
    fun getAllSavedWallPosts(): List<VKWallPostAndAttachments>

    @Query("select * from vksourcemodel")
    fun getGroupInfo(): List<VKSourceModel>

    @Query("select source_id from vkwallpostmodel")
    fun getSavedGroupsId(): List<Int>

    @Query("select * from vksourcemodel where id==:passedId")
    fun getGroupInfoById(passedId: Int): VKSourceModel

    @Query("select vkWallPostId from vkwallpostmodel")
    fun getSavedWallPostIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkWallPost: VKWallPostModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkAttachments: VKAttachments)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkSourceModel: VKSourceModel)

    @Insert
    fun insert(photo: VKSize)

    @Insert
    fun insert(link: VKLink)

    fun insertAttachments(vkAttachments: List<VKAttachments>?, vkWallPostId: String) {
        if (vkAttachments != null)
            for (vkAtt in vkAttachments)
                if (vkAtt.type == "photo" || vkAtt.type == "link") {
                    vkAtt.wallParentAttachments = vkWallPostId
                    insert(vkAtt)
                }
    }

    fun insertLink(link: VKLink?, vkWallPostId: String) {
        if (link != null) {
            link.wallParentLink = vkWallPostId
            insert(link)
            insertPhoto(link.photo, vkWallPostId)
        }
    }

    fun insertPhoto(photos: List<VKSize>?, vkWallPostId: String) {
        if (photos != null)
            for (photo in photos) {
                photo.wallParentSize = vkWallPostId
                insert(photo)
            }
    }


    @Transaction
    fun insertWallPost(wallPost: VKWallPostModel, vkSourceModel: VKSourceModel?) {
        insert(wallPost)
        insertAttachments(wallPost.attachments, wallPost.vkWallPostId)
        if (wallPost.attachments != null)
            for (att in wallPost.attachments!!) {
                insertPhoto(att.photo, wallPost.vkWallPostId)
                insertLink(att.link, wallPost.vkWallPostId)
            }
        if (vkSourceModel != null)
            insert(vkSourceModel)
    }

    @Delete
    fun deleteWallPost(wallPost: VKWallPostModel)

    fun deletePost(wallPost: VKWallPostModel) {
        val id = wallPost.source_id
        deleteWallPost(wallPost)
        if (id !in getSavedGroupsId())
            deleteGroupById(id)
    }


    @Delete
    fun deleteGroup(source: VKSourceModel)

    fun deleteGroupById(id: Int) {
        deleteGroup(getGroupInfoById(abs(id)))
    }
}