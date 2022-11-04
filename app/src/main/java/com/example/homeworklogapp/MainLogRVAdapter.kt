package com.example.homeworklogapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class MainLogRVAdapter (
    private val taskList: ArrayList<Task>, // list of items to populate recycler view with
    private val mapSubjectColor: HashMap<String, Int>, // list of subject-color pairs
    private val listColors: ArrayList<Color> // list of Colors
        ): RecyclerView.Adapter<MainLogRVAdapter.NewViewHolder>() {

            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): NewViewHolder { // inflate the layout for task_rv_item.xml
                val itemView = LayoutInflater.from(parent.context).inflate(
                    R.layout.task_rv_item,
                    parent, false
                )

                return NewViewHolder(itemView, mListener)
            }

    class NewViewHolder(itemView: View, listener: onItemClickListener) :
        RecyclerView.ViewHolder(itemView) { // initialize views
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTask: TextView = itemView.findViewById(R.id.tvTask)
        val tvDueDate: TextView = itemView.findViewById(R.id.tvDueDate)

        val cardView: CardView = itemView.findViewById(R.id.cardView)

        init {
            itemView.setOnClickListener() {
                listener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: NewViewHolder, position: Int) { // populate views with data from list
        val subject = taskList[position].subject
        val context = holder.cardView.context

        holder.tvSubject.text = subject
        holder.tvTask.text = taskList[position].task
        holder.tvDueDate.text = taskList[position].dueDate

        val backgroundColorIndex = mapSubjectColor[subject] // TODO: set background color of task_rv_item according to subject

        if (backgroundColorIndex != null) {
            val color = listColors[backgroundColorIndex].color
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(context, color))
        }
    }

    override fun getItemCount(): Int { // this function is required
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