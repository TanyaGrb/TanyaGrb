package com.fktimp.news

import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel

interface NewsHelperInterface {
    fun onDeleteLoad()
    fun showToast(text: String)
    fun onNewData(
        items: List<VKWallPostModel>,
        srcInfo: List<VKGroupModel>
    )
    fun onError()
}