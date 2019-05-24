package com.ricknout.materialthemingcustomviews

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(R.layout.activity_main), ColorPickerView.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        colorPickerView.callback = this
        colorPickerView.colors = listOf(
            "#004a2f", "#002f35", "#ff6337", "#ffa323",
            "#f7ff56", "#94fc13", "#4be3ac", "#032d3c",
            "#e41749", "#f5587b", "#ff8a5c", "#fff591",
            "#33313b", "#4592af", "#e3c4a8", "#f6f5f5",
            "#bfcd7e", "#ee7777", "#8e2e6a", "#311054"
        )
    }

    override fun onPickColor(color: String) {
        Toast.makeText(this, "Picked color: $color", Toast.LENGTH_SHORT).show()
    }
}
