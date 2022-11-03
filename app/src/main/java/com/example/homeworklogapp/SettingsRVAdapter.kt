package com.example.homeworklogapp

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Spinner
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class SettingsRVAdapter(
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

        holder.setIsRecyclable(false) // prevent "recycling" the views - keeps EditText content/position from being jumbled up

        val subject = listSubjectColor[position].subject
        val colorIndex = listSubjectColor[position].colorIndex

        holder.etSubject.setText(subject)
        holder.etSubject.addTextChangedListener(
            textWatcher(
                holder.etSubject,
                position
            )
        ) // to watch for duplicate subject entries

        holder.spinnerColor.adapter = holder.adapter
        holder.spinnerColor.setSelection(colorIndex)
    }

    override fun getItemCount(): Int {
        return listSubjectColor.size
    }

    class textWatcher(val etSubject: EditText, val position: Int) : TextWatcher {

        val context = etSubject.context

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            val input = p0.toString().trim()

            (context as ActivitySettings).updateListSubject(input, position) // update listSubject to match changes

            (context as ActivitySettings).checkDuplicates() // checkDuplicates() is run in MainActivity so it can iterate through all items in rvList
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }
}