package com.ricknout.materialthemingcustomviews

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.getColorStateListOrThrow
import androidx.core.content.res.getDimensionOrThrow
import androidx.core.content.res.getResourceIdOrThrow
import androidx.core.content.withStyledAttributes
import androidx.core.graphics.ColorUtils
import androidx.core.view.doOnLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.ripple.RippleUtils
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
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

    private val materialShapeDrawable = MaterialShapeDrawable(context, attrs, R.attr.colorPickerStyle, R.style.AppColorPicker)
    private val itemShapeAppearanceModel = ShapeAppearanceModel(context, attrs, R.attr.itemStyle, R.style.AppColorPickerItem)
    private lateinit var itemRippleColor: ColorStateList

    init {
        inflate(context, R.layout.view_color_picker, this)
        colorsRecyclerView.adapter = adapter
        okButton.setOnClickListener {
            val color = selectedColor ?: return@setOnClickListener
            callback?.onPickColor(color)
            selectedColor = null
        }
        context.withStyledAttributes(attrs, R.styleable.ColorPickerView, defStyleAttr, R.style.AppColorPicker) {
            val backgroundTint = getColorStateListOrThrow(R.styleable.ColorPickerView_backgroundTint)
            val elevation = getDimensionOrThrow(R.styleable.ColorPickerView_android_elevation)
            val titleTextColor = getColorStateListOrThrow(R.styleable.ColorPickerView_titleTextColor)
            val subtitleTextColor = getColorStateListOrThrow(R.styleable.ColorPickerView_subtitleTextColor)
            val itemStyleRes = getResourceIdOrThrow(R.styleable.ColorPickerView_itemStyle)
            context.withStyledAttributes(itemStyleRes, R.styleable.ColorPickerItem) {
                itemRippleColor = getColorStateListOrThrow(R.styleable.ColorPickerItem_rippleColor)
            }
            materialShapeDrawable.initializeElevationOverlay(context)
            background = materialShapeDrawable
            backgroundTintList = backgroundTint
            setElevation(elevation)
            titleTextView.setTextColor(titleTextColor)
            subtitleTextView.setTextColor(subtitleTextColor)
        }
    }

    override fun setElevation(elevation: Float) {
        super.setElevation(elevation)
        materialShapeDrawable.elevation = elevation
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
            val rippleColor = RippleUtils.convertToRippleDrawableColor(itemRippleColor)
            val maskDrawable = GradientDrawable().apply { setColor(Color.WHITE) }
            val rippleDrawable = RippleDrawable(rippleColor, null, maskDrawable)
            itemView.background = rippleDrawable
            val tintColor = Color.parseColor(colorItem.color)
            val materialShapeDrawable = MaterialShapeDrawable(itemShapeAppearanceModel)
            itemView.colorView.background = materialShapeDrawable
            itemView.colorView.background.setTint(tintColor)
            itemView.colorView.doOnLayout { view ->
                val cornerRadius = view.width.toFloat() / 2
                materialShapeDrawable.setCornerRadius(cornerRadius)
            }
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
