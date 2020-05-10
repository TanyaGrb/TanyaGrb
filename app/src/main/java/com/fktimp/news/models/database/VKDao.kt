package com.fktimp.news.models.database

import androidx.room.*
import com.fktimp.news.models.VKAttachments
import com.fktimp.news.models.VKLink
import com.fktimp.news.models.VKSize
import com.fktimp.news.models.VKWallPostModel

@Dao
interface VKDao {

    @Query("select * from vkwallpostmodel")
    fun getAll(): List<VKWallPostAndAttachments>

    @Query("select * from vkattachments")
    fun getAtt(): List<VKAttachments>


    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkWallPost: VKWallPostModel)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(vkAttachments: VKAttachments)


    fun insertAttachments(vkAttachments: List<VKAttachments>?, postId: Int) {
        if (vkAttachments != null)
            for (vkAtt in vkAttachments)
                if (vkAtt.type == "photo" || vkAtt.type == "link") {
                    vkAtt.wallParentAttachments = postId
                    insert(vkAtt)
                }
    }

    fun insertLink(link: VKLink?, postId: Int) {
        if (link != null) {
            link.wallParentLink = postId
            insert(link)
            insertPhoto(link.photo, postId)
        }
    }

    fun insertPhoto(photos: List<VKSize>?, postId: Int) {
        if (photos != null)
            for (photo in photos) {
                photo.wallParentSize = postId
                insert(photo)
            }
    }

    @Insert
    fun insert(photo: VKSize)

    @Insert
    fun insert(link: VKLink)

    @Transaction
    fun godInsert(wallPost: VKWallPostModel) {
        insert(wallPost)
        insertAttachments(wallPost.attachments, wallPost.post_id)
        if (wallPost.attachments != null)
            for (att in wallPost.attachments!!) {
                insertPhoto(att.photo, wallPost.post_id)
                insertLink(att.link, wallPost.post_id)
            }
    }

    @Delete
    fun deleteWallPost(wallPost: VKWallPostModel)

    @Query("select post_id from vkwallpostmodel")
    fun getSavedIds(): List<Int>
}