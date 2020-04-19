package com.fktimp.news.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.fktimp.news.R
import com.fktimp.news.models.VKGroupModel
import kotlinx.android.synthetic.main.group_info_item.view.*

class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val photoImageView: ImageView = itemView.group_photo_image_view
    private val groupTitleTextView: CheckedTextView = itemView.group_title_text_view


    fun bind(group: VKGroupModel) {
        Glide.with(itemView.context)
            .load(group.photo_100)
            .circleCrop()
            .into(photoImageView)
        groupTitleTextView.text = group.name
        groupTitleTextView.isChecked = group.isPicked ?: false

        itemView.setOnClickListener {
            groupTitleTextView.isChecked = !groupTitleTextView.isChecked
            group.isPicked = groupTitleTextView.isChecked
        }
    }
}

open class GroupAdapter(val groupList: List<VKGroupModel>) :
    RecyclerView.Adapter<GroupViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.group_info_item, parent, false)
        )
    }

    override fun getItemCount(): Int = groupList.size


    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(groupList[position])
    }
}
