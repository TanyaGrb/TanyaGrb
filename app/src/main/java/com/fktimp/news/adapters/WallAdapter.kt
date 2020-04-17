package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Point
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fktimp.news.R
import com.fktimp.news.SeeMoreTextView
import com.fktimp.news.activities.VKState
import com.fktimp.news.models.Attachments
import com.fktimp.news.models.VKGroupModel
import com.fktimp.news.models.VKWallPostModel
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import com.stfalcon.imageviewer.StfalconImageViewer
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


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
    var photoLayout: FlexboxLayout = itemView.findViewById<View>(R.id.flex_layout) as FlexboxLayout

    init {
        photoLayout.flexDirection = FlexDirection.ROW
        photoLayout.flexWrap = FlexWrap.WRAP
//        photoLayout.alignItems = AlignItems.STRETCH
//        photoLayout.alignContent = AlignContent.SPACE_BETWEEN
        photoLayout.justifyContent = JustifyContent.SPACE_BETWEEN
        photoLayout.setDividerDrawable(
            ContextCompat.getDrawable(
                photoLayout.context,
                R.drawable.divider
            )
        )
        photoLayout.setShowDivider(FlexboxLayout.SHOW_DIVIDER_MIDDLE)
    }
}

class WallAdapter(
    private val activity: Activity,
    var items: List<VKWallPostModel?>,
    var groupsInfo: List<VKGroupModel>
) : RecyclerView.Adapter<ViewHolder>() {

    var screenWidth: Int = 0
    var screenHeight: Int = 0

    init {
        VKState.isVKExist = isPackageInstalled(VK_APP_PACKAGE_ID, activity.packageManager)
        val display: Display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        screenWidth = size.x
        screenHeight = size.y
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
        if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
            return
        }
        if (holder !is ItemViewHolder) {
            return
        }
        val wallPost: VKWallPostModel? = items[position]
        holder.date.text = getDate(wallPost?.date ?: 0)
        if (wallPost?.text.isNullOrBlank()) {
            holder.text.text = ""
        } else {
            holder.text.setContent(wallPost?.text)
        }
        val currentGroup =
            groupsInfo.find { it.id == kotlin.math.abs(wallPost?.source_id ?: 0) }
        holder.title.text = currentGroup?.name ?: "Unknown"
        Glide.with(activity)
            .load(currentGroup?.photo_100)
            .placeholder(R.drawable.ic_group)
            .circleCrop()
            .into(holder.photo)

        holder.photoLayout.removeAllViews()
        if (wallPost == null || wallPost.attachments.isNullOrEmpty()) {
            return
        }

        val countOfImages = wallPost.attachments.count { it.type == "photo" }
        if (countOfImages == 0) {
            return
        }
        val urls =
            List(countOfImages) { wallPost.attachments[it].photo.sizes[wallPost.attachments[it].photo.sizes.lastIndex].url }
        when (countOfImages) {
            1 -> {
                // 1
                val actualHeight =
                    getActualHeight(wallPost.attachments, 0, 0, screenWidth, screenHeight)
                val best =
                    wallPost.attachments[0].photo.sizes[wallPost.attachments[0].photo.sizes.lastIndex]
                val worst = wallPost.attachments[0].photo.sizes[0]
                addPicture(
                    worst.url,
                    screenWidth,
                    actualHeight,
                    best.url,
                    holder.photoLayout,
                    urls
                )
            }
            2 -> {
                // 2
                val actualHeight =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        1,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight
                    )
                for (i: Int in 0..1) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[0].photo.sizes[0]
                    addPicture(
                        worst.url,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        actualHeight,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            3 -> {
                // 1 2
                val actualHeightRow0 =
                    getActualHeight(wallPost.attachments, 0, 0, screenWidth, screenHeight / 2)
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        1,
                        2,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                for (i: Int in 0..2) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i == 0) screenWidth else screenWidth / 2 - DIVIDER_WIDTH,
                        if (i == 0) actualHeightRow0 else actualHeightRow1,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            4 -> {
                // 1 3
                val actualHeightRow0 =
                    getActualHeight(wallPost.attachments, 0, 0, screenWidth, screenHeight / 2)
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        1,
                        3,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                for (i: Int in 0..3) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i == 0) screenWidth else screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        if (i == 0) actualHeightRow0 else actualHeightRow1,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            5 -> {
                // 2 3
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        1,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        2,
                        4,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                for (i: Int in 0..4) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i < 2) screenWidth / 2 - DIVIDER_WIDTH else screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        if (i < 2) actualHeightRow0 else actualHeightRow1,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            6 -> {
                // 3 3
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        2,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        3,
                        5,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 2
                    )
                for (i: Int in 0..5) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        if (i < 3) actualHeightRow0 else actualHeightRow1,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            7 -> {
                // 3 2 2
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        2,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        3,
                        4,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow2 =
                    getActualHeight(
                        wallPost.attachments,
                        5,
                        6,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                for (i: Int in 0..6) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i < 3) screenWidth / 3 - 2 * DIVIDER_WIDTH else screenWidth / 2 - DIVIDER_WIDTH,
                        if (i < 3) actualHeightRow0 else if (i < 5) actualHeightRow1 else actualHeightRow2,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            8 -> {
                // 2 3 3
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        1,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        2,
                        4,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow2 =
                    getActualHeight(
                        wallPost.attachments,
                        5,
                        7,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                for (i: Int in 0..7) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i < 2) screenWidth / 2 - DIVIDER_WIDTH else screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        if (i < 2) actualHeightRow0 else if (i < 5) actualHeightRow1 else actualHeightRow2,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            9 -> {
                // 2 3 4
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        1,
                        screenWidth / 2 - DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        2,
                        4,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow2 =
                    getActualHeight(
                        wallPost.attachments,
                        5,
                        8,
                        screenWidth / 4 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                for (i: Int in 0..8) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i < 2) screenWidth / 2 - DIVIDER_WIDTH else if (i < 5) screenWidth / 3 - 2 * DIVIDER_WIDTH else screenWidth / 4 - 2 * DIVIDER_WIDTH,
                        if (i < 2) actualHeightRow0 else if (i < 5) actualHeightRow1 else actualHeightRow2,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
            10 -> {
                // 4 3 3
                val actualHeightRow0 =
                    getActualHeight(
                        wallPost.attachments,
                        0,
                        3,
                        screenWidth / 4 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow1 =
                    getActualHeight(
                        wallPost.attachments,
                        4,
                        6,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                val actualHeightRow2 =
                    getActualHeight(
                        wallPost.attachments,
                        7,
                        9,
                        screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        screenHeight / 3
                    )
                for (i: Int in 0..9) {
                    val best =
                        wallPost.attachments[i].photo.sizes[wallPost.attachments[i].photo.sizes.lastIndex]
                    val worst = wallPost.attachments[i].photo.sizes[0]
                    addPicture(
                        worst.url,
                        if (i < 4) screenWidth / 4 - 2 * DIVIDER_WIDTH else screenWidth / 3 - 2 * DIVIDER_WIDTH,
                        if (i < 4) actualHeightRow0 else if (i < 7) actualHeightRow1 else actualHeightRow2,
                        best.url,
                        holder.photoLayout,
                        urls
                    )
                }
            }
        }

    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(time: Long): String = SimpleDateFormat("dd MMM HH:mm").format(Date(time * 1000L))

    @SuppressLint("CheckResult")
    private fun addPicture(
        placeholderURL: String,
        actualWidth: Int,
        actualHeight: Int,
        url: String,
        layout: FlexboxLayout,
        urls: List<String>
    ) {
        val imageView = ImageView(layout.context)

        imageView.layoutParams = ViewGroup.LayoutParams(actualWidth, actualHeight)
        val options = RequestOptions()
        if (actualHeight != screenHeight) {
            options.centerCrop()
        }
        Glide.with(layout.context)
            .load(url)
            .thumbnail(
                Glide.with(layout.context)
                    .load(placeholderURL)
            )
            .apply(options)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(imageView)
        layout.addView(imageView)
        imageView.setOnClickListener {
            StfalconImageViewer.Builder(it.context, urls) { view, image ->
                Glide.with(layout.context)
                    .load(image)
                    .into(view)
            }
                .withStartPosition(urls.indexOf(url))
                .withHiddenStatusBar(false)
                .show()

        }
    }

    private fun getActualHeight(
        sourceWidth: Int,
        sourceHeight: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        val ratio =
            maxWidth.toDouble() / sourceWidth
        val actualHeight = (sourceHeight * ratio).roundToInt()
        return if (actualHeight <= maxHeight) actualHeight else maxHeight
    }

    private fun getActualHeight(
        attachments: ArrayList<Attachments>,
        from: Int,
        to: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        val lst: MutableList<Int> = mutableListOf()

        for (i: Int in from..to) {
            val best = attachments[i].photo.sizes[attachments[i].photo.sizes.lastIndex]
            lst.add(getActualHeight(best.width, best.height, maxWidth, maxHeight))
        }
        return lst.min() ?: 0
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
        private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"

        // к каждой стороне будет по разделителю, поэтому эта переменная в два раза меньше
        private const val DIVIDER_WIDTH = 1
    }
}