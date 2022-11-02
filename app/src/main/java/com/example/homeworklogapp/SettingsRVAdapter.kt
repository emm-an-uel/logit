package com.example.homeworklogapp

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SettingsRVAdapter (
    private val listSubjectColor: ArrayList<SubjectColor>, // list of items to populate rv with
    private val listSubject: ArrayList<String>
        ) : RecyclerView.Adapter<SettingsRVAdapter.NewViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.settings_rv_item,
            parent,
            false
        )

        return NewViewHolder(itemView)
    }

    class NewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) { // initialize views

        val etSubject: EditText = itemView.findViewById(R.id.etSubject)

        val context = etSubject.context

        val listColors = arrayListOf(
            R.color.blue,
            R.color.red,
            R.color.yellow,
            R.color.green,
            R.color.pink
        )

        val spinnerColor: Spinner = itemView.findViewById(R.id.spinnerColor)
        val adapter = SettingsSpinnerAdapter(context, listColors)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views with data from list

        val subject = listSubjectColor[position].subject
        val colorIndex = listSubjectColor[position].colorIndex

        holder.etSubject.setText(subject)
        holder.etSubject.addTextChangedListener(
            textWatcher(
                holder.etSubject,
                listSubject
            )
        ) // to watch for duplicate subject entries

        checkForDuplicates(subject, holder.etSubject)
        // TODO: set etSubject text color to red if there are duplicate entries (not exclusively when that editText is being edited - which is what textWatcher does)

        holder.spinnerColor.adapter = holder.adapter
        holder.spinnerColor.setSelection(colorIndex)
    }

    private fun checkForDuplicates(subject: String, etSubject: EditText) {
        // TODO: check if this works; if it does, remove the TODO above too
        val context = etSubject.context
        val defaultColor = etSubject.currentTextColor

    }

    override fun getItemCount(): Int {
        return listSubjectColor.size
    }

    class textWatcher(val etSubject: EditText, val listSubject: ArrayList<String>) : TextWatcher {

        val context = etSubject.context

        val defaultColor = etSubject.currentTextColor

        override fun afterTextChanged(p0: Editable?) {
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            val subject = p0.toString().trim()
            val count = listSubject.count{it == subject}

            if (count > 1) {
                etSubject.setTextColor(ContextCompat.getColor(context, R.color.red)) // sets text color to red

            } else {
                etSubject.setTextColor(defaultColor) // revert to default color
            }
        }
    }
}