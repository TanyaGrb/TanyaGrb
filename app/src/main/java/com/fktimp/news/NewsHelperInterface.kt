package com.fktimp.news

import com.fktimp.news.models.VKSourceModel
import com.fktimp.news.models.VKWallPostModel

interface NewsHelperInterface {
    fun onDeleteLoad()
    fun showToast(text: String)
    fun onNewData(
        items: List<VKWallPostModel>,
        srcInfo: List<VKSourceModel>
    )
    fun onError()
}