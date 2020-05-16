package com.fktimp.news.models.database

import androidx.room.*
import com.fktimp.news.models.*
import kotlin.math.abs

@Dao
interface VKDao {

    @Query("select * from vkwallpostmodel")
    fun getAllSavedWallPosts(): List<VKWallPostAndAttachments>


    @Query("select * from vkgroupmodel")
    fun getGroupInfo(): List<VKGroupModel>

    @Query("select source_id from vkwallpostmodel")
    fun getSavedGroupsId(): List<Int>

    @Query("select * from vkgroupmodel where id==:passedId")
    fun getGroupInfoById(passedId: Int): VKGroupModel

    @Query("select vkWallPostId from vkwallpostmodel")
    fun getSavedWallPostIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkWallPost: VKWallPostModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkAttachments: VKAttachments)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkGroupModel: VKGroupModel)

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
    fun insertWallPost(wallPost: VKWallPostModel, vkGroupModel: VKGroupModel?) {
        insert(wallPost)
        insertAttachments(wallPost.attachments, wallPost.vkWallPostId)
        if (wallPost.attachments != null)
            for (att in wallPost.attachments!!) {
                insertPhoto(att.photo, wallPost.vkWallPostId)
                insertLink(att.link, wallPost.vkWallPostId)
            }
        if (vkGroupModel != null)
            insert(vkGroupModel)
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
    fun deleteGroup(group: VKGroupModel)

    fun deleteGroupById(id: Int) {
        deleteGroup(getGroupInfoById(abs(id)))
    }
}