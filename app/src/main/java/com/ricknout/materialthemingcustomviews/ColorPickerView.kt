package com.ricknout.materialthemingcustomviews

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getColorStateListOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.shape.MaterialShapeDrawable
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
            subtitleTextView.text = value
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
        val materialShapeDrawable = MaterialShapeDrawable(context, attrs, R.attr.colorPickerStyle, R.style.AppColorPicker)
        val styledAttrs = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerView, defStyleAttr, R.style.AppColorPicker)
        val backgroundTint = styledAttrs.getColorStateListOrThrow(R.styleable.ColorPickerView_backgroundTint)
        val elevation = styledAttrs.getDimensionOrThrow(R.styleable.ColorPickerView_android_elevation)
        val titleTextColor = styledAttrs.getColorStateListOrThrow(R.styleable.ColorPickerView_titleTextColor)
        val subtitleTextColor = styledAttrs.getColorStateListOrThrow(R.styleable.ColorPickerView_subtitleTextColor)
        backgroundTintList = backgroundTint
        setElevation(elevation)
        titleTextView.setTextColor(titleTextColor)
        subtitleTextView.setTextColor(subtitleTextColor)
        styledAttrs.recycle()
        background = materialShapeDrawable
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
