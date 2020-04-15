package com.fktimp.news

import android.content.Context
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat


class SeeMoreTextView : AppCompatTextView {
    private var textMaxLength = 250
    private var seeMoreTextColor: Int = R.color.linkColor
    private var collapsedTextWithSeeMoreButton: String? = null
    private var expandedTextWithSeeMoreButton: String? = null
    private var originalContent: String? = null
    private var collapsedTextSpannable: SpannableString? = null
    private var expandedTextSpannable: SpannableString? = null
    private var seeMore = "\nПоказать полностью ..."

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    fun getMoreText() {
        text = expandedTextSpannable
        customAddLinks(this)
    }

    fun setContent(text: String?) {
        originalContent = text
        this.movementMethod = LinkMovementMethod.getInstance()
        if (originalContent!!.length >= textMaxLength) {
            collapsedTextWithSeeMoreButton =
                originalContent!!.substring(0, textMaxLength) + "... " + seeMore
            expandedTextWithSeeMoreButton = "$originalContent"

            collapsedTextSpannable = SpannableString(collapsedTextWithSeeMoreButton)
            expandedTextSpannable = SpannableString(expandedTextWithSeeMoreButton)
            collapsedTextSpannable!!.setSpan(
                clickableSpan,
                textMaxLength + 4,
                collapsedTextWithSeeMoreButton!!.length,
                0
            )
            setText(collapsedTextSpannable)
        } else {
            setText(originalContent)
        }
        customAddLinks(this)
    }

    private var clickableSpan: ClickableSpan = object : ClickableSpan() {
        override fun onClick(widget: View) {
            getMoreText()
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
            ds.color = ContextCompat.getColor(context, seeMoreTextColor)
        }
    }
}