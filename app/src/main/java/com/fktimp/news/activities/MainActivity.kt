package com.fktimp.news.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.NewsHelperInterface
import com.fktimp.news.R
import com.fktimp.news.adapters.OnLoadMoreListener
import com.fktimp.news.adapters.OnSaveWallPostClickListener
import com.fktimp.news.adapters.RecyclerViewLoadMoreScroll
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.fragments.GroupPickBottomSheet
import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.models.database.AppDatabase
import com.fktimp.news.models.database.VKDao
import com.fktimp.news.requests.NewsHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.abs


class MainActivity : AppCompatActivity(), OnSaveWallPostClickListener,
    NewsHelperInterface {

    private val allWallPosts: ArrayList<VKWallPostModel> = ArrayList()
    private val groupsInfo: ArrayList<VKGroupModel> = ArrayList()
    private val pickedCategories: ArrayList<Int> = ArrayList()
    lateinit var adapter: WallAdapter
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private val filteredWallPost: ArrayList<VKWallPostModel?> = ArrayList()
    lateinit var db: AppDatabase
    lateinit var vkDao: VKDao
    private val handler: Handler = Handler()
    private var runnable: Runnable? = null
    private val TAG = "MainActivityNK@("

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        if (savedInstanceState != null) {
            pickedCategories.clear()
            pickedCategories.addAll(savedInstanceState.getIntegerArrayList("pickedCategories") as ArrayList<Int>)
        }
        NewsHelper.actualSources = NewsHelper.getSavedStringSets(this)
        initChips()
        initRecycler(savedInstanceState)
        db = (applicationContext as VKApplication).getDb()
        vkDao = db.getDao()
    }


    private fun initRecycler(savedInstanceState: Bundle?) {
        if (savedInstanceState != null) {
            allWallPosts.addAll(savedInstanceState.getParcelableArrayList("wallPosts")!!)
            groupsInfo.addAll(savedInstanceState.getParcelableArrayList("groupsInfo")!!)
            filteredWallPost.clear()
            if (pickedCategories.isNotEmpty())
                filteredWallPost.addAll(allWallPosts.filter { it.topic_id in pickedCategories })
            else
                filteredWallPost.addAll(allWallPosts)
        } else {
            filteredWallPost.add(null)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WallAdapter(this, this, filteredWallPost, groupsInfo)
        recyclerView.adapter = adapter

        scrollListener =
            RecyclerViewLoadMoreScroll(recyclerView.layoutManager as LinearLayoutManager).apply {
                setOnLoadMoreListener(object : OnLoadMoreListener {
                    override fun onLoadMore() {
                        Log.d("M_MainActivity", "onLoadMore ${scrollListener.isLoading}")
                        filteredWallPost.add(null)
                        adapter.notifyItemInserted(filteredWallPost.lastIndex)
                        NewsHelper.getNewsData(this@MainActivity)
                    }
                })
            }
        recyclerView.addOnScrollListener(scrollListener)
        if (savedInstanceState == null) {
            scrollListener.isLoading = true
            NewsHelper.getNewsData(this)
        }
        refresh_layout.setOnRefreshListener {
            updateFeed()
            refresh_layout.isRefreshing = false
        }
    }

    private fun initChips() {
        for (topic in topicTitles.keys) {
            val currentChip = Chip(this).apply {
                setChipDrawable(
                    ChipDrawable.createFromAttributes(
                        this@MainActivity,
                        null,
                        0,
                        R.style.ChipFilter
                    )
                )
                text = topic
                setOnClickListener { view ->
                    if ((view as Chip).isChecked)
                        pickedCategories.add(topicTitles[topic] ?: 0)
                    else
                        pickedCategories.remove(topicTitles[topic] ?: 0)
                    changeRecyclerCategories()
                }
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(8, 8, 8, 8) }
                if (pickedCategories.isNotEmpty() && pickedCategories.contains(topicTitles[topic]))
                    isChecked = true
            }
            chip_group.addView(currentChip)
        }
    }


    private fun changeRecyclerCategories() {
        filteredWallPost.clear()
        if (pickedCategories.isEmpty())
            filteredWallPost.addAll(allWallPosts)
        else
            filteredWallPost.addAll(allWallPosts.filter { it.topic_id in pickedCategories })
        adapter.notifyDataSetChanged()
        isAllNewsGot()
    }

    private fun deleteLoading() {
        Log.d("M_MainActivity", "deleteLoading ${scrollListener.isLoading}")
        if (!scrollListener.isLoading && filteredWallPost[filteredWallPost.lastIndex] != null) return
        filteredWallPost.removeAt(filteredWallPost.lastIndex)
        adapter.notifyItemRemoved(filteredWallPost.size)
    }

    fun updateFeed() {
        NewsHelper.next_from_news = ""
        allWallPosts.clear()
        filteredWallPost.clear()
        filteredWallPost.add(null)
        adapter.notifyDataSetChanged()
        scrollListener.isLoading = true
        NewsHelper.getNewsData(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("wallPosts", allWallPosts)
        outState.putParcelableArrayList("groupsInfo", groupsInfo)
        outState.putIntegerArrayList("pickedCategories", pickedCategories)
        super.onSaveInstanceState(outState)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_change_sources -> {
            GroupPickBottomSheet().show(supportFragmentManager, "Choose groups")
            Thread {
                Log.d(TAG, vkDao.getAllSavedWallPosts().toString())
            }.start()
            true
        }
        R.id.saved -> {
            startActivityForResult(
                Intent(this, SavedWallPostActivity::class.java),
                FAVORITE_REQUEST_CODE
            )
            true
        }
        R.id.search -> {
            SearchActivity.startFrom(this)
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun insertInDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.insertWallPost(wallPost, groupsInfo.find { it.id == abs(wallPost.source_id) })
        }.start()
    }

    private fun deleteFromDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.deletePost(wallPost)
        }.start()
    }

    override fun onSave(wallPost: VKWallPostModel, isNowChecked: Boolean) {
        if (isNowChecked) {
            wallPost.isSaved = true
            insertInDb(wallPost)
        } else {
            wallPost.isSaved = false
            deleteFromDb(wallPost)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FAVORITE_REQUEST_CODE && resultCode == FAVORITE_UPDATE_CODE)
            for (item in data?.getIntegerArrayListExtra(INTENT_EXTRA_NAME) ?: ArrayList()) {
                var index = allWallPosts.indexOfFirst { it.post_id == item }
                if (index == -1) continue
                allWallPosts[index].isSaved = false
                index = filteredWallPost.indexOfFirst { it?.post_id == item }
                if (index == -1) continue
                filteredWallPost[index]?.isSaved = false
                adapter.notifyItemChanged(index)
            }

    }

    override fun onDeleteLoad() {
        deleteLoading()
        scrollListener.setLoaded()
    }


    override fun showToast(text: String) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()


    override fun onNewData(
        items: List<VKWallPostModel>,
        groupInfo: List<VKGroupModel>
    ) {
        Log.d("M_MainActivity", "onNewData")
        deleteLoading()
        Thread {
            val savedWallPosts = vkDao.getSavedWallPostIds()
            items.forEach {
                if (it.vkWallPostId in savedWallPosts)
                    it.isSaved = true
            }
            runOnUiThread {
                val startPos = filteredWallPost.size
                groupsInfo.addAll(groupInfo)
                groupsInfo.distinct()
                allWallPosts.addAll(items)
                val filteredItems =
                    if (pickedCategories.isNotEmpty()) items.filter { it.topic_id in pickedCategories } else items
                filteredWallPost.addAll(filteredItems)
                adapter.notifyItemRangeInserted(startPos, filteredItems.size)
                Log.d("M_MainActivity", "size = ${filteredWallPost.size}")
                scrollListener.setLoaded()
                isAllNewsGot(needToUpdate = filteredItems.isEmpty())
            }
        }.start()
    }

    override fun onError() {
        Log.d("M_MainActivity", "on error")
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { onDeleteLoad() }
        runnable?.let { handler.postDelayed(it, 2000) }
    }

    private fun isAllNewsGot(needToUpdate: Boolean = false) {
        if ((filteredWallPost.size < NewsHelper.newsAtOnce || needToUpdate) && !NewsHelper.isAllNews()) {
            scrollListener.callOnLoadMore()
            scrollListener.isLoading = true
        } else if (NewsHelper.isAllNews())
            onLastPage()
    }

    private fun onLastPage() {
        //todo показывать фото
        if (filteredWallPost.isEmpty()) {
            Log.d("M_MainActivity", "показывать фото")
        } else
            Log.d("M_MainActivity", "Новостей больше нет")
    }

    companion object {
        fun startFrom(context: Context) {
            val intent = Intent(context, MainActivity::class.java)
            context.startActivity(intent)
        }

        private val topicTitles = mapOf(
            "Арт" to 1,
            "IT" to 7,
            "Игры" to 12,
            "Музыка" to 16,
            "Фото" to 19,
            "Наука" to 21,
            "Спорт" to 23,
            "Туризм" to 25,
            "Кино" to 26,
            "Юмор" to 32,
            "Стиль" to 43
        )
        val FAVORITE_REQUEST_CODE = 0
        val FAVORITE_UPDATE_CODE = 1
        val INTENT_EXTRA_NAME = "deleted_ids"
    }
}