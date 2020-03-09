package com.fktimp.news.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fktimp.news.R
import com.fktimp.news.interfaces.ILoadMore
import com.fktimp.news.models.VKWallPost


internal class LoadingViewHolder(itemView: View) : ViewHolder(itemView) {
    var progressBar: ProgressBar

    init {
        progressBar = itemView.findViewById(R.id.progressBar)
    }
}


internal class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    var data: TextView
    var text: TextView

    init {
        data = itemView.findViewById<View>(R.id.data) as TextView
        text = itemView.findViewById<View>(R.id.text) as TextView
    }
}

class WallAdapter(
    val recyclerView: RecyclerView,
    val activity: Activity,
    var items: List<VKWallPost?>
) : RecyclerView.Adapter<ViewHolder>() {

    val VIEW_TYPE_ITEM = 0
    val VIEW_TYPE_LOADING = 1
    lateinit var loadMore: ILoadMore
    var isLoading: Boolean = false
    var visibleThreshold = 5
    var lastVisibleItem = 0
    var totalItemCount = 0

    init {
        val linearLayoutManager =
            recyclerView.layoutManager as LinearLayoutManager?
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager?.itemCount ?: 0
                lastVisibleItem = linearLayoutManager?.findLastVisibleItemPosition() ?: 0

                if (!isLoading && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                    loadMore.onLoadMore()
                    isLoading = true
                }
            }
        })
    }

    override fun getItemViewType(position: Int): Int {
        return if (items.get(position) == null) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
    }

    fun setLoadFun(loadMore: ILoadMore) {
        this.loadMore = loadMore
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == VIEW_TYPE_ITEM)
            ItemViewHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.item_layout,
                    parent,
                    false
                )
            )
        else
            LoadingViewHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.item_loading,
                    parent,
                    false
                )
            )
    }

    override fun getItemCount(): Int = items.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val post: VKWallPost? = items[position]
            holder.data.text = ("${post?.date}")
            holder.text.text = (post?.text)
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    fun setLoaded() {
        isLoading = false
    }
}