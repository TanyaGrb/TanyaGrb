package com.fktimp.news.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.R
import com.fktimp.news.adapters.OnSaveWallPostClickListener
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.models.VKSourceModel
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.models.database.AppDatabase
import com.fktimp.news.models.database.VKDao
import kotlinx.android.synthetic.main.activity_saved_wall_post.*

class SavedWallPostActivity : AppCompatActivity(), OnSaveWallPostClickListener {
    private lateinit var db: AppDatabase
    private lateinit var vkDao: VKDao
    private val wallPosts: ArrayList<VKWallPostModel> = ArrayList()
    private lateinit var wallAdapter: WallAdapter
    private val groupsInfo: ArrayList<VKSourceModel> = ArrayList()
    private val deletedPosts: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_wall_post)
        setSupportActionBar(toolbar)
        // TODO убрать лишнее
        actionBar?.title = "Сохранённые записи"
        supportActionBar?.title = "Сохранённые записи"
        actionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        db = (applicationContext as VKApplication).getDb()
        vkDao = db.getDao()
        getDataFromDb()
    }

    private fun getDataFromDb() {
        Thread {
            val list = vkDao.getAllSavedWallPosts()
            list.forEach {
                wallPosts.add(it.toVKWallPostModel().apply {
                    this.isSaved = true
                })
            }
            val lst = vkDao.getGroupInfo()
            groupsInfo.addAll(lst)
            runOnUiThread {
                initRecyclerView()
            }
        }.start()
    }

    private fun initRecyclerView() {
        wallAdapter = WallAdapter(this, this, wallPosts, groupsInfo)
        saved_recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wallAdapter
        }
        if (wallPosts.isEmpty())
            showPhoto()
    }

    private fun deleteWallPostFromDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.deletePost(wallPost)
        }.start()
    }

    override fun onSave(wallPost: VKWallPostModel, isNowChecked: Boolean) {
        if (!isNowChecked) {
            deletedPosts.add(wallPost.post_id)
            val pos = wallPosts.indexOf(wallPost)
            deleteWallPostFromDb(wallPost)
            wallPosts.removeAt(pos)
            wallAdapter.notifyItemRemoved(pos)
            setResult(
                MainActivity.FAVORITE_UPDATE_CODE,
                Intent().apply { putExtra(MainActivity.INTENT_EXTRA_NAME, deletedPosts) })
            if (wallPosts.isEmpty())
                showPhoto()
        }
    }

    private fun showPhoto() {
        saved_recycler_view.visibility = View.GONE
        saved_image_nf.visibility = View.VISIBLE
    }

    private fun hidePhoto() {
        saved_recycler_view.visibility = View.VISIBLE
        saved_image_nf.visibility = View.GONE
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
