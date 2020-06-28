package com.fktimp.news.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.NewsHelperInterface
import com.fktimp.news.R
import com.fktimp.news.adapters.OnLoadMoreListener
import com.fktimp.news.adapters.OnSaveWallPostClickListener
import com.fktimp.news.adapters.RecyclerViewLoadMoreScroll
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.custom.DelayedOnQueryTextListener
import com.fktimp.news.models.VKSourceModel
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.models.database.AppDatabase
import com.fktimp.news.models.database.VKDao
import com.fktimp.news.requests.NewsHelper
import kotlinx.android.synthetic.main.activity_search.*
import kotlin.math.abs

class SearchActivity : AppCompatActivity(), OnSaveWallPostClickListener, NewsHelperInterface {
    private val allWallPosts: ArrayList<VKWallPostModel?> = ArrayList()
    private val srcInfo: ArrayList<VKSourceModel> = ArrayList()
    lateinit var adapter: WallAdapter
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private val handler: Handler = Handler()
    private var runnable: Runnable? = null
    lateinit var db: AppDatabase
    lateinit var vkDao: VKDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        db = (applicationContext as VKApplication).getDb()
        vkDao = db.getDao()
        initRecycler()
        search_view.setQuery(intent.getStringExtra("q"), true)
    }

    private fun initRecycler() {
        adapter = WallAdapter(this, this, allWallPosts, srcInfo)
        recycler_view_search.layoutManager = LinearLayoutManager(this)
        recycler_view_search.adapter = adapter
        initSearch()
        scrollListener =
            RecyclerViewLoadMoreScroll(recycler_view_search.layoutManager as LinearLayoutManager).apply {
                setOnLoadMoreListener(object : OnLoadMoreListener {
                    override fun onLoadMore() {
                        Log.d("M_SearchActivity", "onLoadMore ${scrollListener.isLoading}")
                        allWallPosts.add(null)
                        recycler_view_search.post { adapter.notifyItemInserted(allWallPosts.size) }
                        NewsHelper.getSearchNews(search_view.query.toString(), this@SearchActivity)
                    }

                    override fun extraCondition() = !NewsHelper.isAllNewsSearch()
                })
            }
        recycler_view_search.addOnScrollListener(scrollListener)
    }

    private fun initSearch() =
        search_view.setOnQueryTextListener(object : DelayedOnQueryTextListener(search_view) {
            override fun onDelayerQueryTextChange(query: String?) {
                query?.let { getData(it) }
            }
        })


    private fun getData(q: String) {
        if (q.isBlank()) return
        hidePhoto()
        NewsHelper.clearNext(NewsHelper.Next.SEARCH)
        val countOfWallPosts = allWallPosts.size
        allWallPosts.clear()
        srcInfo.clear()
        adapter.notifyItemRangeRemoved(0, countOfWallPosts)
        NewsHelper.getSearchNews(q, this)
    }

    private fun deleteLoading() {
        Log.d("M_SearchActivity", "deleteLoading ${scrollListener.isLoading}")
        if (!scrollListener.isLoading || allWallPosts[allWallPosts.lastIndex] != null) return
        allWallPosts.removeAt(allWallPosts.lastIndex)
        adapter.notifyItemRemoved(allWallPosts.size)
    }

    override fun onDeleteLoad() {
        Log.d("M_SearchActivity", "onDeleteLoad")
        deleteLoading()
        scrollListener.setLoaded()
    }

    override fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()


    override fun onNewData(items: List<VKWallPostModel>, srcInfo: List<VKSourceModel>) {
        Log.d("M_SearchActivity", "onNewData")
        deleteLoading()
        Thread {
            val savedWallPosts = vkDao.getSavedWallPostIds()
            items.forEach {
                if (it.vkWallPostId in savedWallPosts)
                    it.isSaved = true
            }
            runOnUiThread {
                val startPos = allWallPosts.size
                this.srcInfo.addAll(srcInfo)
                this.srcInfo.distinct()
                allWallPosts.addAll(items)
                adapter.notifyItemRangeInserted(startPos, items.size)
                Log.d("M_SearchActivity", "size = ${allWallPosts.size}")
                scrollListener.setLoaded()
                if (allWallPosts.isEmpty())
                    showPhoto()
            }
        }.start()
    }


    override fun onError() {
        Log.d("M_SearchActivity", "onError")
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { onDeleteLoad() }
        runnable?.let { handler.postDelayed(it, 2000) }
    }

    override fun onSave(wallPost: VKWallPostModel, isNowChecked: Boolean) {
        Log.d("M_SearchActivity", "onSave $isNowChecked")
        wallPost.isSaved = isNowChecked
        if (isNowChecked)
            insertInDb(wallPost)
        else
            deleteFromDb(wallPost)
    }

    private fun insertInDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.insertWallPost(wallPost, srcInfo.find { it.id == abs(wallPost.source_id) })
        }.start()
    }

    private fun deleteFromDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.deletePost(wallPost)
        }.start()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        Log.d("M_SearchActivity", "onNewIntent")
        intent?.let { search_view.setQuery(it.getStringExtra("q"), true) }
    }

    private fun showPhoto() {
        recycler_view_search.visibility = View.GONE
        image_search_nf.visibility = View.VISIBLE
    }

    private fun hidePhoto() {
        recycler_view_search.visibility = View.VISIBLE
        image_search_nf.visibility = View.GONE
    }

    companion object {
        fun startFrom(context: Context, q: String = "") {
            context.startActivity(Intent(context, SearchActivity::class.java).apply {
                putExtra("q", q)
            })
        }
    }
}
