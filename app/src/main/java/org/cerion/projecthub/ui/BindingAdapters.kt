package org.cerion.projecthub.ui

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import org.cerion.projecthub.R
import org.cerion.projecthub.model.Card
import org.cerion.projecthub.model.IssueCard
import org.cerion.projecthub.model.NoteCard

@BindingAdapter("cardImage")
fun ImageView.setCardImage(card: Card) {
    val id = when (card) {
        is NoteCard -> R.drawable.card_type_note
        is IssueCard -> {
            if (card.closed)
                R.drawable.card_type_issue_closed
            else
                R.drawable.card_type_issue_open
        }
        else -> throw NotImplementedError()
    }

    setImageDrawable(context.getDrawable(id))
}