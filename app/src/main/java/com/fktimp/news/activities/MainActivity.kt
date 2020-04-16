package com.fktimp.news.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.R
import com.fktimp.news.adapters.OnLoadMoreListener
import com.fktimp.news.adapters.RecyclerViewLoadMoreScroll
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.requests.NewsHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val wallPosts: ArrayList<VKWallPostModel> = ArrayList()
    val groupsInfo: ArrayList<VKGroupModel> = ArrayList()
    lateinit var adapter: WallAdapter
    lateinit var scrollListener: RecyclerViewLoadMoreScroll


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NewsHelper.actualSources = NewsHelper.getSavedStringSets(this)
        initRecycler(savedInstanceState)
    }


    private fun initRecycler(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            wallPosts.addAll(savedInstanceState.getParcelableArrayList("wallPosts")!!)
            groupsInfo.addAll(savedInstanceState.getParcelableArrayList("groupsInfo")!!)
        } else {
            wallPosts.add(VKWallPostModel())
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WallAdapter(this, wallPosts, groupsInfo)
        recyclerView.adapter = adapter
        val linearLayoutManager =
            recyclerView.layoutManager as LinearLayoutManager?


        scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                scrollListener.isLoading = true
                wallPosts.add(VKWallPostModel())
                adapter.notifyItemInserted(wallPosts.size - 1)
                NewsHelper.getData(this@MainActivity)
            }
        })
        recyclerView.addOnScrollListener(scrollListener)
        if (savedInstanceState == null) {
            scrollListener.isLoading = true
            NewsHelper.getData(this)
        }
    }

    fun updateRecycler(items: ArrayList<VKWallPostModel>, groups: ArrayList<VKGroupModel>) {
        deleteLoading()
        val startPos = wallPosts.size
        groupsInfo.addAll(groups)
        groupsInfo.distinct()
        wallPosts.addAll(items)
        adapter.notifyItemRangeInserted(startPos, items.size)
    }

    fun deleteLoading() {
        if (!scrollListener.isLoading){
            return
        }
        wallPosts.removeAt(wallPosts.size - 1)
        adapter.notifyItemRemoved(wallPosts.size)
        scrollListener.setLoaded()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("wallPosts", wallPosts)
        outState.putParcelableArrayList("groupsInfo", groupsInfo)
        super.onSaveInstanceState(outState)
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}
