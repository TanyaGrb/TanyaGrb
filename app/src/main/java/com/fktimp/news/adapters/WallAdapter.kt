package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fktimp.news.R
import com.fktimp.news.models.VKWallPost
import java.text.SimpleDateFormat
import java.util.*


internal class LoadingViewHolder(itemView: View) : ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
}


internal class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    var data: TextView = itemView.findViewById<View>(R.id.data) as TextView
    var text: TextView = itemView.findViewById<View>(R.id.text) as TextView
}

class WallAdapter(
    private val activity: Activity,
    var items: List<VKWallPost?>
) : RecyclerView.Adapter<ViewHolder>() {


    override fun getItemViewType(position: Int): Int {
        return if (items[position]?.source_id == 0) VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
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


    @SuppressLint("SimpleDateFormat")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (holder is ItemViewHolder) {
            val post: VKWallPost? = items[position]
            val time = post?.date ?: 0
            val date = Date(time * 1000L)
            val jdf = SimpleDateFormat("dd MMM HH:mm")

            holder.data.text = jdf.format(date)
            holder.text.text = (post?.text)
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }
}