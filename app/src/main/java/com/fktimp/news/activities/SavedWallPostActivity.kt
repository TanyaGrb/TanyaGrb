package com.fktimp.news.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.R
import com.fktimp.news.adapters.OnSaveWallPostClickListener
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.models.VKWallPostModel
import com.fktimp.news.models.database.AppDatabase
import com.fktimp.news.models.database.VKDao
import kotlinx.android.synthetic.main.activity_saved_wall_post.*

class SavedWallPostActivity : AppCompatActivity(), OnSaveWallPostClickListener {
    private lateinit var db: AppDatabase
    private lateinit var vkDao: VKDao
    private val wallPosts: ArrayList<VKWallPostModel> = ArrayList()
    private lateinit var wallAdapter: WallAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_wall_post)
        db = (applicationContext as VKApplication).getDb()
        vkDao = db.getDao()
        getDataFromDb()
    }

    private fun getDataFromDb() {
        Thread {
            vkDao.getAll().forEach {
                wallPosts.add(it.toVKWallPostModel().apply {
                    this.isSaved = true
                })
            }
            runOnUiThread {
                initRecyclerView()
            }
        }.start()
    }

    private fun initRecyclerView() {
        wallAdapter = WallAdapter(this, this, wallPosts, ArrayList())
        recycler_view.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = wallAdapter
        }
    }

    private fun deleteWallPostFromDb(wallPost: VKWallPostModel) {
        Thread {
            vkDao.deleteWallPost(wallPost)
        }.start()
    }

    override fun onSave(wallPost: VKWallPostModel, isNowChecked: Boolean) {
        if (!isNowChecked) {
            val pos = wallPosts.indexOf(wallPost)
            deleteWallPostFromDb(wallPost)
            wallPosts.removeAt(pos)
            wallAdapter.notifyItemRemoved(pos)
        }
    }
}
