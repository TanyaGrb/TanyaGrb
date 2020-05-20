package com.fktimp.news.requests

import android.content.Context
import android.util.Log
import com.fktimp.news.NewsHelperInterface
import com.fktimp.news.models.VKNewsModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback


object NewsHelper {
    const val SOURCE_SET = "string_set_key"
    const val STOP = "STOP_CONST"
    val defaultSources =
        arrayOf(
            "-61559790",
            "-67531827",
            "-88384060",
            "-91150385",
            "-940543",
            "-192270804",
            "-28905875"
        )
    lateinit var actualSources: Set<String>
    var next_from_news: String = ""
    var next_from_search: String = ""
    const val newsAtOnce = 15

    fun isAllNews() = next_from_news == STOP


    fun saveDefaultSources(context: Context) {
        val sharedPref = context.getSharedPreferences(
            SOURCE_SET, Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putStringSet(SOURCE_SET, defaultSources.toHashSet())
        editor.apply()
    }

    fun deleteAllSources(context: Context) {
        val sharedPref = context.getSharedPreferences(
            SOURCE_SET, Context.MODE_PRIVATE
        )
        sharedPref.edit().clear().apply()
    }

    fun saveStringSet(context: Context, mSet: HashSet<String>) {
        val sharedPref = context.getSharedPreferences(
            SOURCE_SET, Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putStringSet(SOURCE_SET, mSet)
        editor.apply()
    }

    fun getSavedStringSets(context: Context): Set<String> {
        val result = (context.getSharedPreferences(
            SOURCE_SET,
            Context.MODE_PRIVATE
        ).getStringSet(SOURCE_SET, null) as Set<String>)
        val offsets: Map<String, Int> = mutableMapOf()
        for (source in result)
            offsets.plus(Pair(source, 0))
        return result
    }


    fun getNewsData(listener: NewsHelperInterface) {
        Log.d("M_MainActivity", "getNewsData")
        if (next_from_news == STOP || actualSources.isEmpty()) {
            if (actualSources.isEmpty())
                listener.showToast("Не выбран ни один источник.")
            else
                Log.d("M_MainActivity", "Нет новостей")
            listener.onDeleteLoad()
            return
        }
        VK.execute(
            VKNewsRequest(actualSources.joinToString(", "), newsAtOnce, next_from_news),
            object : VKApiCallback<VKNewsModel> {
                override fun fail(error: Exception) {
                    error.message?.let { listener.showToast(it) }
                    listener.onError()
                }

                override fun success(result: VKNewsModel) {
                    next_from_news =
                        if (result.next_from.isNullOrBlank())
                            STOP
                        else result.next_from
                    listener.onNewData(result.items, result.groups)
                }
            })
    }

    fun getSearchNews() {

    }

}