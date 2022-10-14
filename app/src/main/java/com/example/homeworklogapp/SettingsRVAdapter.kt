package com.example.homeworklogapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SettingsRVAdapter (
    private val listSubjectColor: ArrayList<SubjectColor>, // list of items to populate rv with
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
        val etSubject: TextView = itemView.findViewById(R.id.etSubject)

        val listColors = arrayListOf(
                R.color.blue,
                R.color.red,
                R.color.yellow,
                R.color.green,
                R.color.pink
            )

        val spinnerColor: Spinner = itemView.findViewById(R.id.spinnerColor)
        val adapter = SettingsSpinnerAdapter(etSubject.context, listColors)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views with data from list
        holder.etSubject.text = listSubjectColor[position].subject

        holder.spinnerColor.adapter = holder.adapter
        // holder.spinnerColor.selectedItem = TODO: implement: spinner's default selectedItem is the subject's color code
    }

    override fun getItemCount(): Int {
        return listSubjectColor.size
    }

}