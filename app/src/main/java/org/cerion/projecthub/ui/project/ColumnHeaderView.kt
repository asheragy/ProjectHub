package org.cerion.projecthub.ui.project

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.ColumnProperties
import org.cerion.projecthub.R
import org.cerion.projecthub.databinding.ColumnHeaderBinding


class ColumnHeaderView(context: Context, private val viewModel: ColumnViewModel) : LinearLayout(context) {

    private lateinit var binding: ColumnHeaderBinding
    private lateinit var adapter: ItemAdapter

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ColumnHeaderBinding.inflate(inflater, this, true)
        binding.viewModel = viewModel

        // TODO fix this so its not forever
        viewModel.cards.observeForever {
            adapter.itemList = it
        }

    }

    fun getColumnProperties(): ColumnProperties {
        adapter = ItemAdapter()
        val layoutManager = LinearLayoutManager(context)
        //val backgroundColor = ContextCompat.getColor(context, R.color.column_background)

        return ColumnProperties.Builder.newBuilder(adapter)
            .setLayoutManager(layoutManager)
            .setHasFixedItemSize(false)
            .setColumnBackgroundColor(Color.TRANSPARENT)
            //.setItemsSectionBackgroundColor(backgroundColor)
            .setHeader(this)
            //.setColumnDragView(header)
            .build()
    }
}