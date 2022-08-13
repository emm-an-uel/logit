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
                    R.layout.task_rv_item,
                    parent, false
                )

                return NewViewHolder(itemView, mListener)
            }

    class NewViewHolder(itemView: View, listener: onItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTask: TextView = itemView.findViewById(R.id.tvTask)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        init {
            itemView.setOnClickListener() {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) {
        holder.tvSubject.text = taskList[position].subject
        holder.tvTask.text = taskList[position].task
        holder.tvDueDate.text = taskList[position].dueDate
    }

    override fun getItemCount(): Int {
        return taskList.size
    }

    // click listener

    private lateinit var mListener: onItemClickListener

    interface onItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: onItemClickListener) {
        mListener = listener
    }
}