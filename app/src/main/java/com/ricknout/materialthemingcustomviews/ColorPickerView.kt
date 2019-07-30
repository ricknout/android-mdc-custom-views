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
import androidx.annotation.ColorInt
import androidx.annotation.Dimension
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
import com.google.android.material.shape.CornerFamily
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

    var shapeAppearance = ShapeAppearanceModel()
        set(value) {
            field = value
            materialShapeDrawable.shapeAppearanceModel = value
        }

    fun setShapeAppearance(@CornerFamily cornerFamily: Int, @Dimension cornerSize: Int) {
        shapeAppearance = ShapeAppearanceModel().apply { setAllCorners(cornerFamily, cornerSize) }
    }

    fun setBackgroundTint(@ColorInt color: Int) {
        backgroundTintList = ColorStateList.valueOf(color)
    }

    var titleTextColor = ColorStateList(emptyArray(), intArrayOf())
        set(value) {
            field = value
            titleTextView.setTextColor(value)
        }

    fun setTitleTextColor(@ColorInt color: Int) {
        titleTextColor = ColorStateList.valueOf(color)
    }

    var subtitleTextColor = ColorStateList(emptyArray(), intArrayOf())
        set(value) {
            field = value
            subtitleTextView.setTextColor(value)
        }

    fun setSubtitleTextColor(@ColorInt color: Int) {
        subtitleTextColor = ColorStateList.valueOf(color)
    }

    var itemShapeAppearance = ShapeAppearanceModel()
        set(value) {
            field = value
            adapter.notifyItemRangeChanged(0, colors.size)
        }

    fun setItemShapeAppearance(@CornerFamily cornerFamily: Int) {
        itemShapeAppearance = ShapeAppearanceModel().apply { setAllCorners(cornerFamily, ShapeAppearanceModel.PILL) }
    }

    var itemRippleColor = ColorStateList(emptyArray(), intArrayOf())
        set(value) {
            field = value
            adapter.notifyItemRangeChanged(0, colors.size)
        }

    fun setItemRippleColor(@ColorInt color: Int) {
        itemRippleColor = ColorStateList.valueOf(color)
    }

    private val materialShapeDrawable = MaterialShapeDrawable()

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
        background = materialShapeDrawable
        materialShapeDrawable.initializeElevationOverlay(context)
        context.withStyledAttributes(attrs, R.styleable.ColorPickerView, defStyleAttr, R.style.Widget_App_ColorPickerView) {
            val shapeAppearanceResId = getResourceIdOrThrow(R.styleable.ColorPickerView_shapeAppearance)
            shapeAppearance = ShapeAppearanceModel(context, shapeAppearanceResId, 0)
            backgroundTintList = getColorStateListOrThrow(R.styleable.ColorPickerView_android_backgroundTint)
            val elevation = getDimensionOrThrow(R.styleable.ColorPickerView_android_elevation)
            setElevation(elevation)
            titleTextColor = getColorStateListOrThrow(R.styleable.ColorPickerView_titleTextColor)
            subtitleTextColor = getColorStateListOrThrow(R.styleable.ColorPickerView_subtitleTextColor)
            val itemShapeAppearanceResId = getResourceIdOrThrow(R.styleable.ColorPickerView_itemShapeAppearance)
            itemShapeAppearance = ShapeAppearanceModel(context, itemShapeAppearanceResId, 0)
            itemRippleColor = getColorStateListOrThrow(R.styleable.ColorPickerView_itemRippleColor)
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

        @Suppress("RestrictedApi")
        fun bind(colorItem: ColorItem) {
            val rippleColor = RippleUtils.convertToRippleDrawableColor(itemRippleColor)
            val maskDrawable = GradientDrawable().apply { setColor(Color.WHITE) }
            val rippleDrawable = RippleDrawable(rippleColor, null, maskDrawable)
            itemView.background = rippleDrawable
            itemView.isSelected = colorItem.selected
            val tintColor = Color.parseColor(colorItem.color)
            val materialShapeDrawable = MaterialShapeDrawable(itemShapeAppearance)
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
