package org.cerion.projecthub.ui.project

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.woxthebox.draglistview.ColumnProperties
import org.cerion.projecthub.databinding.ColumnFooterBinding


class ColumnFooterView(context: Context, viewModel: ColumnViewModel) : LinearLayout(context) {

    private lateinit var binding: ColumnFooterBinding

    init {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        binding = ColumnFooterBinding.inflate(inflater, this, true)
        binding.viewModel = viewModel
    }
}