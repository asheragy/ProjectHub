package org.cerion.projecthub.ui

import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.ImageView
import android.widget.TextView
import org.cerion.projecthub.R
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.DraftIssueCard
import org.cerion.projecthub.model.IssueCard


fun ImageView.setCardImage(card: Card) {
    val id = when (card) {
        is DraftIssueCard -> R.drawable.card_type_draft_issue
        is IssueCard -> {
            if (card.closed)
                R.drawable.card_type_issue_closed
            else
                R.drawable.card_type_issue_open
        }
    }

    setImageDrawable(context.getDrawable(id))
}

fun setFormattedText(textView: TextView, text: String) {
    // Replace markdown emojis
    val sb = SpannableStringBuilder(replaceMarkdown(text))
    var start = 0

    // Bold text formatting
    while(true) {
        val tempText = sb.toString()
        start = tempText.indexOf("**", start)
        val end = tempText.indexOf("**", start + 1)
        if (start == -1 || end == -1)
            break

        sb.setSpan(StyleSpan(Typeface.BOLD), start, end, Spannable.SPAN_INCLUSIVE_INCLUSIVE)
        sb.replace(start, start+2, "")
        sb.replace(end-2, end, "")
        start = end + 2
    }

    textView.text = sb
}

// TODO more efficient way of doing this by searching for tags rather than 100+ replaces
fun replaceMarkdown(str: String): String {
    var result = str
    result = result.replace(":sparkles:", getEmojiByUnicode(0x2728))

    return result
}

fun getEmojiByUnicode(unicode: Int) = String(Character.toChars(unicode))
