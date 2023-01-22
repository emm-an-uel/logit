package com.example.logit.addtask

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.content.ContextCompat
import com.example.logit.R

class AddTaskSpinnerAdapter(context: Context, subjects: List<String>) :
    ArrayAdapter<String>(context, 0, subjects) {

    private val numSubjects = subjects.size

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return myView(position, convertView, parent, dropDown = false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return myView(position, convertView, parent, dropDown = true)
    }

    private fun myView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        dropDown: Boolean
    ): View {
        val subject: String? = getItem(position)
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.spinner_item_custom, parent, false)

        if (view.background != null) {
            view.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    android.R.color.transparent
                )
            ) // get rid of default bg
        }

        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val background: LinearLayout = view.findViewById(R.id.background)

        if (subject != null) {
            tvSubject.text = subject
        } else {
            tvSubject.visibility = View.GONE
        }

        // set transparent background if it's the current option being chosen (aesthetic)
        if (!dropDown) {
            background.background = getDrawable(context, android.R.color.transparent)

        } else { // special conditions for first and last items
            if (position == 0) {
                background.background = getDrawable(context, R.drawable.custom_spinner_bg_top)
            } else if (position == numSubjects - 1) {
                background.background = getDrawable(context, R.drawable.custom_spinner_bg_bottom)
            }
        }

        return view
    }
}