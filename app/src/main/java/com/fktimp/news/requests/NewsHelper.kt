package com.fktimp.news.requests

import android.content.Context
import android.widget.Toast
import com.fktimp.news.activities.MainActivity
import com.fktimp.news.models.VKNewsModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback


object NewsHelper {
    const val SOURCE_SET = "string_set_key"
    const val STOP = "STOP_CONST"
    val defaultSources =
        arrayOf("-61559790", "-67531827", "-88384060", "-91150385", "-940543", "-192270804")
    lateinit var actualSources: Set<String>
    var next_from: String = ""


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


    fun getData(context: Context) {
        if (next_from == STOP || actualSources.isEmpty()) {
            Toast.makeText(context, "Новостей больше нет", Toast.LENGTH_SHORT).show()
            (context as MainActivity).deleteLoading()
            return
        }
        VK.execute(
            VKNewsRequest(actualSources.joinToString(", "), 15, next_from),
            object : VKApiCallback<VKNewsModel> {
                override fun fail(error: Exception) {
                    Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
                }

                override fun success(result: VKNewsModel) {
                    if (result.items.isEmpty() && next_from.isEmpty())
                        Toast.makeText(
                            context,
                            "Новостей нет.Попробуйте изменить список источников.",
                            Toast.LENGTH_LONG
                        ).show()
                    (context as MainActivity).updateRecycler(result.items, result.groups)
                    next_from = if (result.next_from.isNullOrBlank()) STOP else result.next_from

                }
            })
    }

}