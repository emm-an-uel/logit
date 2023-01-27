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
        return newView(position, convertView, parent, dropDown = false)
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return newView(position, convertView, parent, dropDown = true)
    }

    private fun newView(
        position: Int,
        convertView: View?,
        parent: ViewGroup,
        dropDown: Boolean
    ): View {
        val subject: String? = getItem(position)
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.spinner_item_custom, parent, false)

        val tvSubject: TextView = view.findViewById(R.id.tvSubject)
        val background: LinearLayout = view.findViewById(R.id.background)
        val dividerLine: View = view.findViewById(R.id.dividerLine)

        tvSubject.text = subject

        // special conditions for selected item (not in drop-down list) 
        if (!dropDown) {
            background.background = getDrawable(context, android.R.color.transparent)
            dividerLine.visibility = View.GONE
            tvSubject.setTextColor(ContextCompat.getColor(context, R.color.white))

        } else { // special conditions for first and last items in drop-down list 
            if (position == 0) {
                background.background = getDrawable(context, R.drawable.custom_spinner_bg_top)
            } else if (position == numSubjects - 1) {
                background.background = getDrawable(context, R.drawable.custom_spinner_bg_bottom)
                dividerLine.visibility = View.GONE
            }
        }

        return view
    }
}