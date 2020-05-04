package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Point
import android.net.Uri
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
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
    var date: TextView = itemView.findViewById(R.id.date) as TextView
    var text: SeeMoreTextView = itemView.findViewById(R.id.text) as SeeMoreTextView
    var title: TextView = itemView.findViewById(R.id.link_title) as TextView
    var photo: ImageView = itemView.findViewById(R.id.photo) as ImageView
    var photoLayout: FlexboxLayout = itemView.findViewById(R.id.flex_layout) as FlexboxLayout
    var link: ConstraintLayout =
        itemView.findViewById(R.id.link) as ConstraintLayout
    var mainLayout: LinearLayout =
        itemView.findViewById(R.id.main_layout) as LinearLayout
    var linkTitle: TextView = link.findViewById(R.id.link_title) as TextView
    var linkCaption: TextView = link.findViewById(R.id.caption) as TextView
    var linkImage: ImageView = link.findViewById(R.id.link_image) as ImageView

    init {
        photoLayout.flexDirection = FlexDirection.ROW
        photoLayout.flexWrap = FlexWrap.WRAP
        photoLayout.setDividerDrawable(
            ContextCompat.getDrawable(
                photoLayout.context,
                R.drawable.divider
            )
        )
        photoLayout.setShowDivider(FlexboxLayout.SHOW_DIVIDER_MIDDLE)
        linkImage.layoutParams.width = 140
        linkImage.layoutParams.height = 100
    }
}

class WallAdapter(
    private val activity: Activity,
    private var items: List<VKWallPostModel?>,
    private var groupsInfo: List<VKGroupModel>
) : RecyclerView.Adapter<ViewHolder>() {

    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

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
                LayoutInflater.from(parent.context).inflate(
                    R.layout.wall_post_item_layout,
                    parent,
                    false
                )
            )
        else
            LoadingViewHolder(
                LayoutInflater.from(parent.context).inflate(
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
            holder.text.visibility = GONE
        } else {
            holder.text.visibility = VISIBLE
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

        holder.link.visibility = GONE
        if (wallPost == null || wallPost.attachments.isNullOrEmpty()) {
            return
        }
        val attachedImages = wallPost.attachments.asSequence().filter { it.type == "photo" }
            .map(VKAttachments::photo).toList()
        val attachedLink =
            wallPost.attachments.asSequence().filter { it.type == "link" }.map(VKAttachments::link)
                .toList()
        if (attachedImages.isEmpty() && attachedLink.isEmpty()) {
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

        if (attachedLink.isEmpty()) {
            return
        }
        if (attachedLink[0].title.isBlank()) {
            holder.linkTitle.visibility = GONE
        } else {
            holder.linkTitle.visibility = VISIBLE
        }
        if (attachedLink[0].caption.isBlank()) {
            holder.linkCaption.visibility = GONE
        } else {
            holder.linkCaption.visibility = VISIBLE
        }
        holder.link.visibility = VISIBLE

        holder.linkTitle.text = attachedLink[0].title
        holder.linkCaption.text = attachedLink[0].caption
        holder.link.setOnClickListener { openURL(it.context, attachedLink[0].url) }
        Glide.with(holder.mainLayout.context)
            .load(if (attachedLink[0].photo.sizes.isEmpty()) R.drawable.ic_photo_placeholder else attachedLink[0].photo.sizes[0].url)
            .into(holder.linkImage)
    }

    private fun setImages(
        itemIndex: Int,
        attachedImages: List<VKPhoto>,
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
        var currentRow = -1
        var actualWidth: Int
        for (pictureIndex: Int in 0..attachedImages.lastIndex) {
            if (getRow(placementScheme, pictureIndex) != currentRow) {
                currentRow += 1
                val realWidth =
                    sizeScheme[currentRow].width * placementScheme[currentRow] + (placementScheme[currentRow] - 1) * DIVIDER_WIDTH
                actualWidth = if (realWidth != screenWidth) {
                    screenWidth - (sizeScheme[currentRow].width * (placementScheme[currentRow] - 1) + DIVIDER_WIDTH * 2 * (placementScheme[currentRow] - 1))
                } else {
                    sizeScheme[currentRow].width
                }
            } else {
                actualWidth = sizeScheme[currentRow].width
            }
            val best =
                attachedImages[pictureIndex].sizes[attachedImages[pictureIndex].sizes.lastIndex]
            val worst = attachedImages[pictureIndex].sizes[0]
            addPicture(
                worst.url,
                actualWidth,
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
            val postURLs = items[itemIndex]!!.attachments.asSequence()
                .filter { vkAttachments -> vkAttachments.type == "photo" }
                .map(VKAttachments::photo).map(
                    VKPhoto::sizes
                ).map { list -> list[list.lastIndex].url }.toList()
            lateinit var viewer: StfalconImageViewer<String>
            viewer = StfalconImageViewer.Builder(
                it.context, postURLs
            ) { view, image ->
                Glide.with(layout.context)
                    .load(image)
                    .dontAnimate()
                    .into(view)
            }
                .withTransitionFrom(imageView)
                .withStartPosition(imageIndex)
                .withImageChangeListener { pos ->
                    viewer.updateTransitionImage(layout.getChildAt(pos) as ImageView?)
                }
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
        attachments: List<VKPhoto>,
        from: Int,
        to: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        val lst: MutableList<Int> = mutableListOf()

        for (i: Int in from..to) {
            val best = attachments[i].sizes[attachments[i].sizes.lastIndex]
            lst.add(getActualHeight(best.width, best.height, maxWidth, maxHeight))
        }
        return lst.min() ?: 0
    }

    private fun openURL(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
        private const val VK_APP_PACKAGE_ID = "com.vkontakte.android"

        // к каждой стороне будет по разделителю, поэтому эта переменная в два раза меньше
        private const val DIVIDER_WIDTH = 2
    }
}