package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.fktimp.news.R
import com.fktimp.news.custom.SeeMoreTextView
import com.fktimp.news.models.*
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.stfalcon.imageviewer.StfalconImageViewer
import kotlinx.android.synthetic.main.link_layout.view.*
import kotlinx.android.synthetic.main.wall_post_item_layout.view.*
import kotlinx.android.synthetic.main.wall_post_item_layout.view.link
import kotlinx.android.synthetic.main.wall_post_item_layout.view.src_title
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt

class WallPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private var date: TextView = itemView.date
    private var text: SeeMoreTextView = itemView.text
    private var title: TextView = itemView.src_title
    private var photo: ImageView = itemView.photo
    private var photoLayout: FlexboxLayout = itemView.flex_layout
    private var link: ConstraintLayout = itemView.link as ConstraintLayout
    private var mainLayout: LinearLayout = itemView.main_layout as LinearLayout
    private var linkTitle: TextView = link.src_title
    private var linkCaption: TextView = link.caption
    private var linkImage: ImageView = link.link_image
    private var saveToggle: ToggleButton = itemView.add_button

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

    @Suppress("UNCHECKED_CAST")
    fun bind(
        wallPost: VKWallPostModel?,
        group: VKGroupModel?,
        listener: OnSaveWallPostClickListener
    ) {
        date.text = getDate(wallPost?.date ?: 0)
        if (wallPost?.text.isNullOrBlank()) {
            text.visibility = View.GONE
        } else {
            text.visibility = View.VISIBLE
            text.setContent(wallPost?.text)
        }
        title.text = group?.name ?: "Unknown"
        Glide.with(photo.context)
            .load(group?.photo_100)
            .placeholder(R.drawable.ic_group)
            .circleCrop()
            .into(photo)
        photoLayout.removeAllViews()
        link.visibility = View.GONE
        saveToggle.setOnCheckedChangeListener(null)
        if (wallPost == null) return
        saveToggle.isChecked = wallPost.isSaved
        saveToggle.setOnCheckedChangeListener { _: CompoundButton, b: Boolean ->
            listener.onSave(wallPost, b)
        }
        if (wallPost.attachments == null) return
        val attachedImages: List<List<VKSize>> =
            wallPost.attachments!!.asSequence()
                .filter { it.type == "photo" && it.photo != null }
                .map(VKAttachments::photo).toList() as List<List<VKSize>>
        val attachedLink = wallPost.attachments!!.find { it.type == "link" }?.link
        if (attachedImages.isNotEmpty())
            bindImages(attachedImages)
        if (attachedLink != null)
            bindLink(attachedLink)
    }

    private fun bindImages(attachedImages: List<List<VKSize>>) {
        when (attachedImages.size) {
            1 -> setImages(attachedImages, arrayOf(1))
            2 -> setImages(attachedImages, arrayOf(2))
            3 -> setImages(attachedImages, arrayOf(1, 2))
            4 -> setImages(attachedImages, arrayOf(1, 3))
            5 -> setImages(attachedImages, arrayOf(2, 3))
            6 -> setImages(attachedImages, arrayOf(3, 3))
            7 -> setImages(attachedImages, arrayOf(3, 2, 2))
            8 -> setImages(attachedImages, arrayOf(2, 3, 3))
            9 -> setImages(attachedImages, arrayOf(2, 3, 4))
            10 -> setImages(attachedImages, arrayOf(4, 3, 3))
        }
    }

    private fun bindLink(attachedLink: VKLink) {
        linkTitle.visibility = if (attachedLink.title.isBlank()) View.GONE else View.VISIBLE
        linkCaption.visibility = if (attachedLink.caption.isBlank()) View.GONE else View.VISIBLE
        link.visibility = View.VISIBLE
        linkTitle.text = attachedLink.title
        linkCaption.text = attachedLink.caption
        link.setOnClickListener { openURL(it.context, attachedLink.url) }
        val bestImage = getBestSize(attachedLink.photo)
        Glide.with(mainLayout.context)
            .load(bestImage?.url ?: R.drawable.ic_photo_placeholder)
            .into(linkImage)
    }

    private fun setImages(
        attachedImages: List<List<VKSize>>,
        placementScheme: Array<Int>
    ) {
        val rowsCount = placementScheme.size
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
        val pictureURLs: List<String> = List(attachedImages.size) {
            getBestSize(attachedImages[it])!!.url
        }
        for (pictureIndex: Int in 0..attachedImages.lastIndex) {
            if (getRow(placementScheme, pictureIndex) != currentRow) {
                currentRow += 1
                val realWidth =
                    sizeScheme[currentRow].width * placementScheme[currentRow] + (placementScheme[currentRow] - 1) * WallAdapter.DIVIDER_WIDTH
                actualWidth = if (realWidth != WallAdapter.screenWidth) {
                    WallAdapter.screenWidth - (sizeScheme[currentRow].width * (placementScheme[currentRow] - 1) + WallAdapter.DIVIDER_WIDTH * 2 * (placementScheme[currentRow] - 1))
                } else {
                    sizeScheme[currentRow].width
                }
            } else {
                actualWidth = sizeScheme[currentRow].width
            }
            val best = getBestSize(attachedImages[pictureIndex])!!
            val worst = getWorstSize(attachedImages[pictureIndex])
            addImageToFlexbox(
                worst?.url ?: "",
                actualWidth,
                actualHeight[currentRow],
                best.url,
                photoLayout,
                pictureURLs,
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
                WallAdapter.screenWidth / placementScheme[it] - WallAdapter.DIVIDER_WIDTH * (placementScheme[it] - 1),
                WallAdapter.screenHeight / placementScheme.size
            )
        }


    @SuppressLint("CheckResult")
    private fun addImageToFlexbox(
        placeholderURL: String,
        actualWidth: Int,
        actualHeight: Int,
        url: String,
        layout: FlexboxLayout,
        pictureURLs: List<String>,
        imageIndex: Int
    ) {
        val imageView = ImageView(layout.context)

        imageView.layoutParams =
            ViewGroup.LayoutParams(actualWidth, actualHeight)
        val options = RequestOptions()
        if (actualHeight != WallAdapter.screenHeight) {
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
            lateinit var viewer: StfalconImageViewer<String>
            viewer = StfalconImageViewer.Builder(
                it.context, pictureURLs
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
        attachments: List<List<VKSize>>,
        from: Int,
        to: Int,
        maxWidth: Int,
        maxHeight: Int
    ): Int {
        val lst: MutableList<Int> = mutableListOf()

        for (i: Int in from..to) {
            val best = getBestSize(attachments[i])!!
            lst.add(getActualHeight(best.width, best.height, maxWidth, maxHeight))
        }
        return lst.min() ?: 0
    }

    private fun openURL(context: Context, url: String) {
        val intent =
            Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        context.startActivity(intent)
    }

    private fun getBestSize(sizes: List<VKSize>?): VKSize? = when {
        sizes.isNullOrEmpty() -> null
        sizes.size > 1 -> sizes[1]
        else -> sizes[0]
    }


    private fun getWorstSize(sizes: List<VKSize>): VKSize? = when {
        sizes.isEmpty() -> null
        else -> sizes[0]
    }


    @SuppressLint("SimpleDateFormat")
    fun getDate(time: Long): String =
        SimpleDateFormat("dd MMM HH:mm")
            .format(Date(time * 1000L))
}