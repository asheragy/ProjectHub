package org.cerion.projecthub.ui.project

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.ColumnProperties
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ColumnFooterBinding
import org.cerion.projecthub.databinding.ColumnHeaderBinding


class ColumnHeaderView(context: Context, viewModel: ColumnViewModel) : LinearLayout(context) {

    private lateinit var binding: ColumnHeaderBinding
    private val adapter = ItemAdapter()
    private lateinit var footer: View // TODO this is wrong to define here but need to move column stuff first

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ColumnHeaderBinding.inflate(inflater, this, true)
        binding.viewModel = viewModel

        // TODO fix this so its not forever
        viewModel.cards.observeForever {
            adapter.itemList = it
        }

        footer = ColumnFooterView(context, viewModel)
    }

    fun getColumnProperties(): ColumnProperties {
        val layoutManager = LinearLayoutManager(context)
        //val backgroundColor = ContextCompat.getColor(context, R.color.column_background)

        return ColumnProperties.Builder.newBuilder(adapter)
            .setLayoutManager(layoutManager)
            .setHasFixedItemSize(false)
            .setColumnBackgroundColor(Color.TRANSPARENT)
            //.setItemsSectionBackgroundColor(backgroundColor)
            .setHeader(this)
            .setFooter(footer)
            //.setColumnDragView(header)
            .build()
    }
}