package com.fktimp.news.custom
import android.content.Intent
import android.net.Uri
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.MovementMethod
import android.text.style.URLSpan
import android.view.View
import android.widget.TextView
import androidx.core.util.PatternsCompat.AUTOLINK_EMAIL_ADDRESS
import androidx.core.util.PatternsCompat.AUTOLINK_WEB_URL
import com.fktimp.news.activities.SearchActivity
import com.fktimp.news.activities.VKState
import java.util.regex.Pattern

class OpenLinkSpan(url: String) : URLSpan(url) {
    override fun onClick(widget: View) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        widget.context.startActivity(intent)
    }
}
class OpenSearchSpan(val q: String) : URLSpan(q) { override fun onClick(widget: View) = SearchActivity.startFrom(widget.context, q) }
fun customAddLinks(textView: TextView) {
    val isVKExists = VKState.isVKExist
    val spannable = SpannableStringBuilder.valueOf(textView.text)
    val patternVK = Pattern.compile("""\[(club|id)\d+\|[^]]+]""")
    val patternTags = Pattern.compile("""#[^\s]+""")
    val patternURL = AUTOLINK_WEB_URL
    val patternEmail = AUTOLINK_EMAIL_ADDRESS
    val result = addLinksByPattern(spannable, patternVK, isVKExists, true, emptyArray(), false) or addLinksByPattern(spannable, patternEmail, isVKExists, false, arrayOf("mailto:"), false) or addLinksByPattern(spannable, patternURL, isVKExists, false, arrayOf("http://", "https://", "rtsp://"), false) or addLinksByPattern(spannable, patternTags, isVKExists, false, emptyArray(), true)
    if (result) {
        textView.text = spannable
        val movementMethod: MovementMethod? = textView.movementMethod
        if (movementMethod == null || movementMethod !is LinkMovementMethod) if (textView.linksClickable) textView.movementMethod = LinkMovementMethod.getInstance()
    }
}
private fun applyLink(url: String, start: Int, end: Int, text: Spannable) = text.setSpan(OpenLinkSpan(url), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
private fun applySearch(q: String, start: Int, end: Int, text: Spannable) = text.setSpan(OpenSearchSpan(q), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
fun addLinksByPattern(spannable: SpannableStringBuilder, pattern: Pattern, isVKExists: Boolean, isVK: Boolean, prefixes: Array<String>, isSearch: Boolean): Boolean {
    var localSpannable = spannable
    var m = pattern.matcher(spannable)
    var flag = false
    while (m.find()) {
        val start = m.start()
        val end = m.end()
        val result: String = m.group(0) ?: ""
        val url = if (isVK) getVKUrl(result.substring(1, result.indexOf("|")), isVKExists)
        else makeUrl(result, prefixes)
        val txt: String = if (isVK) result.substring(result.indexOf("|") + 1, result.length - 1) else result
        localSpannable = localSpannable.replace(start, end, txt)
        if (isVK) m = pattern.matcher(localSpannable)
        if (isSearch) applySearch(url, start, start + txt.length, localSpannable)
        else applyLink(url, start, start + txt.length, localSpannable)
        flag = true
    }
    return flag
}
fun getVKUrl(id: String, isVK: Boolean) = if (isVK) "vk://profile/" + if (id.contains("club")) id.replace("club", "-") else id.replace("id", "") else "http://vk.com/$id"
private fun makeUrl(_url: String, prefixes: Array<String>): String {
    if (_url.startsWith("#")) return _url
    var url = _url
    var hasPrefix = false
    for (i in prefixes.indices) {
        if (url.regionMatches(0, prefixes[i], 0, prefixes[i].length, ignoreCase = true)) {
            hasPrefix = true
            if (!url.regionMatches(0, prefixes[i], 0, prefixes[i].length, ignoreCase = false)) url = prefixes[i] + url.substring(prefixes[i].length)
            break
        }
    }
    if (!hasPrefix && prefixes.isNotEmpty()) url = prefixes[0] + url
    return url
}