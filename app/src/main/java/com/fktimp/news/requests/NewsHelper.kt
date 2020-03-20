package com.fktimp.news.requests

import android.content.Context
import android.widget.Toast
import com.fktimp.news.MainActivity
import com.fktimp.news.models.VKExecuteWallModel
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException


object NewsHelper {
    const val SOURCE_SET = "string_set_key"
    private val defaultSources =
        arrayOf("-50246288", "-45715576", "-27775663", "-181445782", "-35684557")
    lateinit var actualSources: Set<String>
    lateinit var offsets: Map<String, Int>


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
        Toast.makeText(context, "All preferences $SOURCE_SET deleted", Toast.LENGTH_SHORT).show()
        saveDefaultSources(context)
    }

    fun saveStringSet(context: Context, mSet: HashSet<String>) {
        val sharedPref = context.getSharedPreferences(
            SOURCE_SET, Context.MODE_PRIVATE
        )
        val editor = sharedPref.edit()
        editor.putStringSet(SOURCE_SET, mSet)
        editor.apply()
        Toast.makeText(context, "Default sources saved", Toast.LENGTH_SHORT).show()
    }

    fun getSavedStringSets(context: Context): Set<String> {
        val result = (context.getSharedPreferences(
            SOURCE_SET,
            Context.MODE_PRIVATE
        ).getStringSet(SOURCE_SET, null) as Set<String>)
        offsets = mutableMapOf()
        for (source in result)
            offsets.plus(Pair(source, 0))
        return result
    }


    fun getData(context: Context) {
        VK.execute(VKExecuteWall(getCode(15)), object : VKApiCallback<VKExecuteWallModel> {
            override fun fail(error: VKApiExecutionException) {
                Toast.makeText(context, error.message, Toast.LENGTH_LONG).show()
            }

            override fun success(result: VKExecuteWallModel) {
                (context as MainActivity).updateRecycler(result.wall)
            }
        })
    }

    private fun getCode(count: Int): String {

        val result: StringBuffer = StringBuffer()

        for ((index, element) in actualSources.withIndex()) {
            result.append("var post$index = API.wall.get({\"owner_id\": $element, \"count\": $count});\n")
        }
        result.append("var allItems = ")
        for (index in actualSources.indices) {
            result.append("post$index.items +")
        }
        result.replace(result.length - 1, result.length, ";\n")
        result.append(SORTING)
        result.append("return {\"wall\": allItems.slice(0,$count), \"extra\":45};")
        println(result.toString().trim())
        return result.toString().trim()

//        return """var posts1 = API.wall.get({"count": $count});
//            var posts2 = API.wall.get({"owner_id":"1","count":$count});
//            var allItems = posts1.items + posts2.items;
//            var i = 0;
//            var len = allItems.length;
//            while (i < len){
//                var j = 0;
//                while (j < len - i - 1){
//                    if (allItems[j].date <= allItems[j + 1].date){
//                        var temp = allItems[j];
//                        allItems.splice(j, 1, allItems[j+1]);
//                        allItems.splice(j + 1, 1, temp);
//                    }
//                    j = j + 1;
//                }
//                i = i + 1;
//            }
//            return {"wall": allItems.slice(0,$count), "extra":45};""".trim()
    }

    const val SORTING =
        "var i = 0; var len = allItems.length; while (i < len){ var j = 0; while (j < len - i - 1)" +
                "{ if (allItems[j].date <= allItems[j + 1].date){ var temp = allItems[j]; " +
                "allItems.splice(j, 1, allItems[j+1]); allItems.splice(j + 1, 1, temp);} " +
                "j = j + 1;} i = i + 1;}"
}