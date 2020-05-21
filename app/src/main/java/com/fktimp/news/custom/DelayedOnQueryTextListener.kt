package com.fktimp.news.custom

import android.os.Handler
import androidx.appcompat.widget.SearchView

abstract class DelayedOnQueryTextListener(private val searchView: SearchView) :
    SearchView.OnQueryTextListener {
    private val handler: Handler = Handler()
    private var runnable: Runnable? = null

    override fun onQueryTextSubmit(s: String?): Boolean {
        runnable?.let {
            if (!handler.hasCallbacks(it)) {
                searchView.clearFocus()
                return true
            }
        }
        runnable?.let { handler.removeCallbacks(it) }
        onDelayerQueryTextChange(s)
        searchView.clearFocus()
        return true
    }

    override fun onQueryTextChange(s: String?): Boolean {
        runnable?.let { handler.removeCallbacks(it) }
        runnable = Runnable { onDelayerQueryTextChange(s) }
        runnable?.let { handler.postDelayed(it, 650) }
        return true
    }

    abstract fun onDelayerQueryTextChange(query: String?)
}