package com.example.logit.settings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.example.logit.R

class SpinnerAdapterSettings (
    context: Context,
    listColors: ArrayList<Int>
): ArrayAdapter<Int>(context, 0, listColors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return myView(position, convertView, parent)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return myView(position, convertView, parent)
    }

    private fun myView(position: Int, convertView: View?, parent: ViewGroup): View {

        val color = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(
            R.layout.spinner_item_color,
            parent,
            false
        )

        color?.let {
            val ivColor = view.findViewById<ImageView>(R.id.ivColor)

            if(color != null) {
                ivColor.setColorFilter(ContextCompat.getColor(context, color))
            }
        }

        return view
    }
}