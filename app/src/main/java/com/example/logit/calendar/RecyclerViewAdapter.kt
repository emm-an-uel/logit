package com.example.logit.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.log.CardColor

class RecyclerViewAdapter (
    private val tasks: List<Task>,
    private val mapSubjectColor: Map<String, Int>,
    private val cardColors: List<CardColor>
): RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        var checked = false
        val ivColorCode: ImageView = itemView.findViewById(R.id.colorCode)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)

        init {
            checkIcon.setOnClickListener {
                checkColors()
                listener.onItemClick(adapterPosition, checked)
            }
        }

        private fun checkColors() {
            if (!checked) {
                checked = true
                checkIcon.imageTintList = ColorStateList.valueOf(getColor(itemView.context, androidx.appcompat.R.attr.colorAccent))
            } else {
                checked = false
                checkIcon.imageTintList = ColorStateList.valueOf(getColor(itemView.context, R.attr.calendarDialogCheckColor))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.calendar_task_card_item, parent, false)
        return ViewHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        val context = holder.tvSubject.context
        holder.tvSubject.text = task.subject
        holder.tvTaskName.text = tasks[position].task

        // set color coded tab
        val bgColorIndex: Int? = mapSubjectColor[task.subject]
        val bgColor: Int = if (bgColorIndex != null) {
            ContextCompat.getColor(context, cardColors[bgColorIndex].backgroundColor)
        } else {
            ContextCompat.getColor(context, R.color.gray)
        }
        holder.ivColorCode.imageTintList = ColorStateList.valueOf(bgColor)
    }

    private fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    // click listener
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int, checked: Boolean)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}