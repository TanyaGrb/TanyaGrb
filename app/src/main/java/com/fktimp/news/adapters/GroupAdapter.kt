package com.fktimp.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fktimp.news.R
import com.fktimp.news.models.VKSourceModel
import kotlinx.android.synthetic.main.group_info_item.view.*

class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val photoImageView: ImageView = itemView.group_photo_image_view
    private val groupTitleTextView: CheckedTextView = itemView.group_title_text_view


    fun bind(source: VKSourceModel) {
        Glide.with(itemView.context)
            .load(source.photo_100)
            .circleCrop()
            .into(photoImageView)
        groupTitleTextView.text = source.name
        groupTitleTextView.isChecked = source.isPicked ?: false

        itemView.setOnClickListener {
            groupTitleTextView.isChecked = !groupTitleTextView.isChecked
            source.isPicked = groupTitleTextView.isChecked
        }
    }
}

open class GroupAdapter(val sourceList: List<VKSourceModel>) :
    RecyclerView.Adapter<GroupViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.group_info_item, parent, false)
        )
    }

    override fun getItemCount(): Int = sourceList.size


    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(sourceList[position])
    }
}
