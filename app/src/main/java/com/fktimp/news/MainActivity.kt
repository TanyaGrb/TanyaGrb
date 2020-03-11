package com.fktimp.news

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.fktimp.news.adapters.WallAdapter
import com.fktimp.news.interfaces.ILoadMore
import com.fktimp.news.models.VKWall
import com.fktimp.news.models.VKWallPost
import com.fktimp.news.requests.VKWallsRequest
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import com.vk.api.sdk.exceptions.VKApiExecutionException
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        VK.setCredentials(
            this,
            7273146,
            "85a7c4e485a7c4e485a7c4e4f285c93e5e885a785a7c4e4dbb3670b90806f21f42228bf",
            null,
            false
        )
        button.setOnClickListener { requestWall(-940543, 15) }
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun requestWall(id: Int, count: Int) {
        VK.execute(VKWallsRequest(id, count), object : VKApiCallback<List<VKWall>> {
            override fun fail(error: VKApiExecutionException) {
                Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
            }

            override fun success(result: List<VKWall>) {
                if (!isFinishing && result.isNotEmpty())
                    initRecycler(ArrayList(result[0].items))
            }
        })
    }

    fun initRecycler(items: ArrayList<VKWallPost?>) {
        val adapter = WallAdapter(recyclerView, this, items)
        recyclerView.adapter = adapter
        adapter.loadMore = object : ILoadMore {
            override fun onLoadMore() {
                Toast.makeText(applicationContext, "OnLoadMore", Toast.LENGTH_SHORT).show()
                adapter.setLoaded()
                items.add(null)
                adapter.notifyItemInserted(items.size - 1)
            }
        }
    }

}