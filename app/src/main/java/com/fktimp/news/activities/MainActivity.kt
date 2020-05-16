package com.fktimp.news.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.R
import com.fktimp.news.adapters.OnLoadMoreListener
import com.fktimp.news.adapters.OnSaveWallPostClickListener
import com.fktimp.news.adapters.RecyclerViewLoadMoreScroll
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.models.database.AppDatabase
import com.fktimp.news.models.database.VKDao
import com.fktimp.news.requests.NewsHelper
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipDrawable
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), OnSaveWallPostClickListener {

    private val allWallPosts: ArrayList<VKWallPostModel> = ArrayList()
    private val groupsInfo: ArrayList<VKGroupModel> = ArrayList()
    private val pickedCategories: ArrayList<Int> = ArrayList()
    lateinit var adapter: WallAdapter
    lateinit var scrollListener: RecyclerViewLoadMoreScroll
    private val filteredWallPost: ArrayList<VKWallPostModel> = ArrayList()
    lateinit var db: AppDatabase
    lateinit var vkDao: VKDao
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
            filteredWallPost.add(VKWallPostModel())
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = WallAdapter(this, this, filteredWallPost, groupsInfo)
        recyclerView.adapter = adapter

        scrollListener = RecyclerViewLoadMoreScroll(LinearLayoutManager(this))
        scrollListener.setOnLoadMoreListener(object : OnLoadMoreListener {
            override fun onLoadMore() {
                scrollListener.isLoading = true
                filteredWallPost.add(VKWallPostModel())
                adapter.notifyItemInserted(filteredWallPost.size - 1)
                NewsHelper.getData(this@MainActivity)
            }
        })
        recyclerView.addOnScrollListener(scrollListener)
        if (savedInstanceState == null) {
            scrollListener.isLoading = true
            NewsHelper.getData(this)
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

    fun updateRecyclerNewInfo(items: ArrayList<VKWallPostModel>, groups: ArrayList<VKGroupModel>) {
        deleteLoading()
        Thread {
            val savedWallPosts = vkDao.getSavedIds()
            items.forEach {
                if (it.post_id in savedWallPosts)
                    it.isSaved = true
            }
            runOnUiThread {
                val startPos = filteredWallPost.size
                groupsInfo.addAll(groups)
                groupsInfo.distinct()
                allWallPosts.addAll(items)
                val filteredItems =
                    if (pickedCategories.isNotEmpty()) items.filter { it.topic_id in pickedCategories } else items
                filteredWallPost.addAll(filteredItems)
                adapter.notifyItemRangeInserted(startPos, filteredItems.size)
            }
        }.start()
    }

    private fun changeRecyclerCategories() {
        filteredWallPost.clear()
        if (pickedCategories.isEmpty())
            filteredWallPost.addAll(allWallPosts)
        else
            filteredWallPost.addAll(allWallPosts.filter { it.topic_id in pickedCategories })
        adapter.notifyDataSetChanged()
    }

    fun deleteLoading() {
        if (!scrollListener.isLoading) {
            return
        }
        filteredWallPost.removeAt(filteredWallPost.size - 1)
        adapter.notifyItemRemoved(filteredWallPost.size)
        scrollListener.setLoaded()
    }

    fun updateFeed() {
        NewsHelper.next_from = ""
        allWallPosts.clear()
        filteredWallPost.clear()
        filteredWallPost.add(VKWallPostModel())
        adapter.notifyDataSetChanged()
        scrollListener.isLoading = true
        NewsHelper.getData(this)
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
                Log.d(TAG, vkDao.getAll().toString())
            }.start()
            true
        }
        R.id.saved -> {
            startActivity(Intent(this, SavedWallPostActivity::class.java))
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    private fun insertInDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.godInsert(wallPost)
        }.start()
    }

    private fun deleteFromDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.deleteWallPost(wallPost)
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
    }
}