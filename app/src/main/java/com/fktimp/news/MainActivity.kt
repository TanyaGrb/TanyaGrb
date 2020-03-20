package com.fktimp.news

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.adapters.OnLoadMoreListener
import com.fktimp.news.adapters.RecyclerViewLoadMoreScroll
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.models.VKWallPost
import com.fktimp.news.requests.NewsHelper
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val wallPosts: ArrayList<VKWallPost> = ArrayList()
    lateinit var adapter: WallAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        NewsHelper.actualSources = NewsHelper.getSavedStringSets(this)
        initRecycler()
        button.setOnClickListener {
            //            requestWall(-940543, 15)
            NewsHelper.getData(this)
        }
    }

    private fun initRecycler() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WallAdapter(this, wallPosts)
        recyclerView.adapter = adapter
        val linearLayoutManager =
            recyclerView.layoutManager as LinearLayoutManager?

        val scrollListener = RecyclerViewLoadMoreScroll(linearLayoutManager as LinearLayoutManager)
        scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                Toast.makeText(applicationContext, "OnLoadMore", Toast.LENGTH_SHORT).show()
                wallPosts.add(VKWallPost())
                adapter.notifyItemInserted(wallPosts.size - 1)
                // get data
                Handler().postDelayed({
                    val temp = ArrayList<VKWallPost>()
                    val start = adapter.itemCount
                    for (i in start..start + 15)
                        temp.add(VKWallPost(id = i))
                    if (wallPosts.isNotEmpty()) {
                        wallPosts.removeAt(wallPosts.size - 1)
                        adapter.notifyItemRemoved(wallPosts.size)
                    }

                    wallPosts.addAll(temp)
                    scrollListener.setLoaded()
                    // TODO исправить способ оповещения
                    adapter.notifyDataSetChanged()
                }, 3000)
            }
        })
        recyclerView.addOnScrollListener(scrollListener)
    }

    fun updateRecycler(items: ArrayList<VKWallPost>) {
        wallPosts.addAll(items)
        // TODO исправить способ оповещения
        adapter.notifyDataSetChanged()
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }
    }
}