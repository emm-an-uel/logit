package com.example.homeworklogapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class RVAdapter (
    private val taskList: ArrayList<Task>,
        ): RecyclerView.Adapter<RVAdapter.NewViewHolder>() {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): NewViewHolder {
                val itemView = LayoutInflater.from(parent.context).inflate(
                    R.layout.fragment_todo,
                    parent, false
                )

                return NewViewHolder(itemView)
            }

    class NewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTask: TextView = itemView.findViewById(R.id.tvTask)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) {
        holder.tvSubject.text = taskList[position].subject
        holder.tvTask.text = taskList[position].task
        holder.tvDueDate.text = taskList[position].dueDate
    }

    override fun getItemCount(): Int {
        return taskList.size
    }
}