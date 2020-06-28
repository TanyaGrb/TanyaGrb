package com.fktimp.news.fragments

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fktimp.news.R
import com.fktimp.news.activities.MainActivity
import com.fktimp.news.adapters.GroupAdapter
import com.fktimp.news.models.VKSourceModel
import com.fktimp.news.requests.NewsHelper
import com.fktimp.news.requests.VKGroupsById
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.vk.api.sdk.VK
import com.vk.api.sdk.VKApiCallback
import kotlinx.android.synthetic.main.pick_sources_bottom_sheet.*
import java.util.*


class GroupPickBottomSheet : BottomSheetDialogFragment() {

    lateinit var adapter: GroupAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.pick_sources_bottom_sheet, container, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.AppBottomSheetDialogTheme)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        if (savedInstanceState?.getParcelableArrayList<VKSourceModel>("groupList") != null) {
            adapter = GroupAdapter(savedInstanceState.getParcelableArrayList("groupList")!!)
        } else {
            getData(group_recyclerview.context)
        }
        group_recyclerview.apply {
            adapter =
                if (this@GroupPickBottomSheet::adapter.isInitialized) this@GroupPickBottomSheet.adapter else
                    GroupAdapter(emptyList())
            layoutManager = LinearLayoutManager(this.context, RecyclerView.VERTICAL, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val bottomSheet =
                    (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
                BottomSheetBehavior.from<View>(bottomSheet).apply {
                    state = BottomSheetBehavior.STATE_EXPANDED
                }
                view.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })
    }

    private fun getData(context: Context) {
        val picked = NewsHelper.getSavedStringSets(context).map { it.substring(1) }
        val allSources = NewsHelper.defaultSources.map { it.substring(1) } + picked
        allSources.distinct()
        VK.execute(VKGroupsById(allSources.joinToString(", ")), object :
            VKApiCallback<List<VKSourceModel>> {
            override fun success(result: List<VKSourceModel>) {
                for (group in picked) {
                    result.find { it.id.toString() == group }?.isPicked = true
                }
                setData(result)
            }

            override fun fail(error: Exception) {
                Toast.makeText(context, "${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


    fun setData(data: List<VKSourceModel>) {
        adapter = GroupAdapter(data)
        group_recyclerview.adapter = adapter
        group_recyclerview.addItemDecoration(
            DividerItemDecoration(
                context,
                DividerItemDecoration.VERTICAL
            )
        )
    }


    override fun onDismiss(dialog: DialogInterface) {
        this.context?.let {
            val current = adapter.sourceList.asSequence().filter { model -> model.isPicked == true }
                .map { model -> "-${model.id}" }.toHashSet()
            if (current != NewsHelper.getSavedStringSets(it)) {
                NewsHelper.deleteAllSources(it)
                NewsHelper.saveStringSet(
                    it, current
                )
                NewsHelper.actualSources = NewsHelper.getSavedStringSets(it)
                (activity as MainActivity).updateFeed()
            }
        }
        super.onDismiss(dialog)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("groupList", adapter.sourceList as ArrayList<out Parcelable>)
    }
}