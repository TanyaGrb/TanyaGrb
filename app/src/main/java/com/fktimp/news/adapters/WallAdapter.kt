package com.fktimp.news.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.fktimp.news.CustomURLSpan
import com.fktimp.news.R
import com.fktimp.news.models.VKWallPost
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


internal class LoadingViewHolder(itemView: View) : ViewHolder(itemView) {
    var progressBar: ProgressBar = itemView.findViewById(R.id.progressBar)
}


internal class ItemViewHolder(itemView: View) : ViewHolder(itemView) {
    var date: TextView = itemView.findViewById<View>(R.id.date) as TextView
    var text: TextView = itemView.findViewById<View>(R.id.text) as TextView

    fun customAddLinks(
        text: TextView
    ) {
        val pattern = Pattern.compile("""\[(club|id)\d+\|(\w|\s)+]""")
        var spannable = SpannableStringBuilder.valueOf(text.text)
        var m = pattern.matcher(spannable)
        var flag = false
        while (m.find()) {
            val start = m.start()
            val end = m.end()
            val result: String = m.group(0) ?: ""
            var url: String = "vk://profile/" + result.substring(1, result.indexOf("|"))
            url = if (url.contains("club"))
                url.replace("club", "-") else
                url.replace("id", "")
            val txt: String = result.substring(result.indexOf("|") + 1, result.length - 1)
            spannable = spannable.replace(start, end, txt)
            m = pattern.matcher(spannable)
            customApplyLink(url, start, start + txt.length, spannable)
            flag = true
        }
        if (flag) {
            text.text = spannable
            val movementMethod: MovementMethod? = text.movementMethod
            if (movementMethod == null || movementMethod !is LinkMovementMethod) {
                if (text.linksClickable)
                    text.movementMethod = LinkMovementMethod.getInstance()
            }
        }
    }

    private fun customApplyLink(
        url: String,
        start: Int,
        end: Int,
        text: Spannable
    ) {
        val span = CustomURLSpan(url)
        text.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
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

            holder.date.text = jdf.format(date)
            holder.text.text = (post?.text)
            holder.customAddLinks(holder.text)
        } else if (holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }


    companion object {
        private const val VIEW_TYPE_ITEM = 0
        const val VIEW_TYPE_LOADING = 1
    }
}