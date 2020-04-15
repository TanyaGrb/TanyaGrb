package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.fktimp.news.R
import com.fktimp.news.SeeMoreTextView
import com.fktimp.news.VKState
import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel
import java.text.SimpleDateFormat
import java.util.*

fun isPackageInstalled(
    packageName: String,
    packageManager: PackageManager
): Boolean {
    return try {
        packageManager.getPackageInfo(packageName, 0)
        true
    } catch (e: PackageManager.NameNotFoundException) {
        false
    }
}

internal class LoadingViewHolder(itemView: View) : ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
}


internal class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    var date: TextView = itemView.findViewById<View>(R.id.date) as TextView
    var text: SeeMoreTextView = itemView.findViewById<View>(R.id.text) as SeeMoreTextView
    var title: TextView = itemView.findViewById<View>(R.id.title) as TextView
    var photo: ImageView = itemView.findViewById<View>(R.id.photo) as ImageView
}

class WallAdapter(
    private val activity: Activity,
    var items: List<VKWallPostModel?>,
    var groupsInfo: List<VKGroupModel>
) : RecyclerView.Adapter<ViewHolder>() {

    init {
        VKState.isVKExist = isPackageInstalled(VK_APP_PACKAGE_ID, activity.packageManager)
    }

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
            val wallPost: VKWallPostModel? = items[position]
            val time = wallPost?.date ?: 0
            val date = Date(time * 1000L)
            val jdf = SimpleDateFormat("dd MMM HH:mm")

            holder.date.text = jdf.format(date)
            holder.text.setContent(wallPost?.text)
            val currentGroup =
                groupsInfo.find { it.id == kotlin.math.abs(wallPost?.source_id ?: 0) }
            holder.title.text = currentGroup?.name ?: "Unknown"
            Glide.with(activity)
                .load(currentGroup?.photo_100)
                .placeholder(R.drawable.ic_group)
                .circleCrop()
                .into(holder.photo)
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
        private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"
    }
}