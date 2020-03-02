package com.fktimp.news

import android.content.Intent
import android.net.Uri
import android.text.style.URLSpan
import android.view.View

class CustomURLSpan(url: String) : URLSpan(url) {
    override fun onClick(widget: View) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(url)
        )
        widget.context.startActivity(intent)
    }
}