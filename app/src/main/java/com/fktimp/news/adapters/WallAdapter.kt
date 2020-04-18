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
import com.fktimp.news.models.*
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
        val attachedImages = wallPost.attachments.filter { it.type == "photo" }
        val attachedLinks = wallPost.attachments.filter { it.type == "link" }
        if (attachedImages.isEmpty() && attachedLinks.isEmpty()) {
            return
        }

        when (attachedImages.size) {
            1 -> {
                // 1
                setImages(position, attachedImages, 1, arrayOf(1), holder.photoLayout)
            }
            2 -> {
                // 2
                setImages(position, attachedImages, 1, arrayOf(2), holder.photoLayout)
            }
            3 -> {
                // 1 2
                setImages(position, attachedImages, 2, arrayOf(1, 2), holder.photoLayout)
            }
            4 -> {
                // 1 3
                setImages(position, attachedImages, 2, arrayOf(1, 3), holder.photoLayout)
            }
            5 -> {
                // 2 3
                setImages(position, attachedImages, 2, arrayOf(2, 3), holder.photoLayout)
            }
            6 -> {
                // 3 3
                setImages(position, attachedImages, 2, arrayOf(3, 3), holder.photoLayout)
            }
            7 -> {
                // 3 2 2
                setImages(position, attachedImages, 3, arrayOf(3, 2, 2), holder.photoLayout)
            }

            8 -> {
                // 2 3 3
                setImages(position, attachedImages, 3, arrayOf(2, 3, 3), holder.photoLayout)
            }
            9 -> {
                // 2 3 4
                setImages(position, attachedImages, 3, arrayOf(2, 3, 4), holder.photoLayout)
            }
            10 -> {
                // 4 3 3
                setImages(position, attachedImages, 3, arrayOf(4, 3, 3), holder.photoLayout)
            }
        }
    }

    private fun setImages(
        itemIndex: Int,
        attachedImages: List<VKAttachments>,
        rowsCount: Int,
        placementScheme: Array<Int>,
        layout: FlexboxLayout
    ) {
        val sizeScheme: Array<PhotoCharacteristics> = getSizeScheme(placementScheme)
        val actualHeight = IntArray(rowsCount)
        var index = 0
        for (rowIndex: Int in 0 until rowsCount) {
            actualHeight[rowIndex] = getActualHeight(
                attachedImages,
                index,
                index + placementScheme[rowIndex] - 1,
                sizeScheme[rowIndex].width,
                sizeScheme[rowIndex].height
            )
            index = placementScheme[rowIndex]
        }
        var currentRow: Int
        for (pictureIndex: Int in 0..attachedImages.lastIndex) {
            currentRow = getRow(placementScheme, pictureIndex)
            val best =
                attachedImages[pictureIndex].photo.sizes[attachedImages[pictureIndex].photo.sizes.lastIndex]
            val worst = attachedImages[pictureIndex].photo.sizes[0]
            addPicture(
                worst.url,
                sizeScheme[currentRow].width,
                actualHeight[currentRow],
                best.url,
                layout,
                itemIndex,
                pictureIndex
            )
        }
    }

    private fun getRow(placementScheme: Array<Int>, currentPhoto: Int): Int {
        var sum = 0
        for ((index, countOfPhotosInRow) in placementScheme.withIndex()) {
            sum += countOfPhotosInRow
            if (currentPhoto + 1 <= sum) {
                return index
            }
        }
        return -1
    }

    private fun getSizeScheme(placementScheme: Array<Int>): Array<PhotoCharacteristics> =
        Array(placementScheme.size) {
            PhotoCharacteristics(
                screenWidth / placementScheme[it] - DIVIDER_WIDTH * (placementScheme[it] - 1),
                screenHeight / placementScheme.size
            )
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
        itemIndex: Int,
        imageIndex: Int
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
            StfalconImageViewer.Builder(it.context,
                items[itemIndex]!!.attachments.asSequence()
                    .filter { vkAttachments -> vkAttachments.type == "photo" }
                    .map(VKAttachments::photo).map(
                        VKPhoto::sizes
                    ).map { list -> list[list.lastIndex].url }.toList()
            ) { view, image ->
                Glide.with(layout.context)
                    .load(image)
                    .into(view)
            }
                .withTransitionFrom(imageView)
                .withStartPosition(imageIndex)
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
        attachments: List<VKAttachments>,
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