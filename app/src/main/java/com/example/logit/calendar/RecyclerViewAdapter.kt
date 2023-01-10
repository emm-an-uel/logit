package com.example.logit.calendar

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.mainlog.CardColor

class RecyclerViewAdapter (
    private val tasks: List<Task>,
    private val mapSubjectColor: Map<String, Int>,
    private val cardColors: List<CardColor>
): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val backgroundLayout: LinearLayout = itemView.findViewById(R.id.backgroundLayout)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.calendar_task_card_item, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val subject = tasks[position].subject
        val context = holder.tvSubject.context
        holder.tvSubject.text = subject
        holder.tvTaskName.text = tasks[position].task

        // set color coded tab
        val bgColorIndex: Int? = mapSubjectColor[subject]
        val bgColor: Int = if (bgColorIndex != null) {
            ContextCompat.getColor(context, cardColors[bgColorIndex].backgroundColor)
        } else {
            ContextCompat.getColor(context, R.color.gray)
        }
        holder.cardView.backgroundTintList = ColorStateList.valueOf(bgColor)
    }
}