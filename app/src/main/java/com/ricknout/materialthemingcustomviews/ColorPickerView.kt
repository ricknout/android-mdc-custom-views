package com.ricknout.materialthemingcustomviews

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.list_item_color_picker.view.*
import kotlinx.android.synthetic.main.view_color_picker.view.*

class ColorPickerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    interface Callback {
        fun onPickColor(color: String)
    }

    var callback: Callback? = null

    var colors = listOf<String>()
        set(value) {
            field = value
            val colorItems = colors.map { color -> ColorItem(color = color) }
            adapter.submitList(colorItems)
        }

    private val adapter = Adapter()

    private var selectedColor: String? = null
        set(value) {
            field = value
            selectedColorTextView.text = value
            val colorItems = colors.map { color -> ColorItem(color = color, selected = color == value) }
            adapter.submitList(colorItems)
        }

    init {
        inflate(context, R.layout.view_color_picker, this)
        colorsRecyclerView.adapter = adapter
        okButton.setOnClickListener {
            val color = selectedColor ?: return@setOnClickListener
            callback?.onPickColor(color)
            selectedColor = null
        }
        setBackgroundResource(R.drawable.bg_color_picker)
    }

    inner class Adapter : ListAdapter<ColorItem, ViewHolder>(DIFFER) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_color_picker, parent, false))

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val color = getItem(position)
            holder.bind(color)
        }
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(colorItem: ColorItem) {
            val tintColor = Color.parseColor(colorItem.color)
            itemView.view.background.setTint(tintColor)
            itemView.selectedImageView.isVisible = colorItem.selected
            val blackContrast = ColorUtils.calculateContrast(Color.BLACK, tintColor)
            val whiteContrast = ColorUtils.calculateContrast(Color.WHITE, tintColor)
            itemView.selectedImageView.setColorFilter(if (blackContrast > whiteContrast) Color.BLACK else Color.WHITE)
            itemView.setOnClickListener {
                selectedColor = colorItem.color
            }
        }
    }

    companion object {
        val DIFFER = object : DiffUtil.ItemCallback<ColorItem>() {
            override fun areItemsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
                return oldItem.color == newItem.color
            }
            override fun areContentsTheSame(oldItem: ColorItem, newItem: ColorItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}
