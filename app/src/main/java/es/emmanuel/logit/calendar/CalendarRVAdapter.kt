package es.emmanuel.logit.calendar

import android.content.Context
import android.content.res.ColorStateList
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import es.emmanuel.logit.ParentActivity
import es.emmanuel.logit.Task

class CalendarRVAdapter (
    private val tasks: ArrayList<Task>,
    private val mapSubjectColor: Map<String, Int>,
    private val colors: List<Int>,
    private val showCompletedTasks: Boolean
): RecyclerView.Adapter<CalendarRVAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View, listener: OnItemClickListener) : RecyclerView.ViewHolder(itemView) {
        val ivColorCode: ImageView = itemView.findViewById(R.id.colorCode)
        val tvSubject: TextView = itemView.findViewById(R.id.tvSubject)
        val tvTaskName: TextView = itemView.findViewById(R.id.tvTaskName)
        val checkIcon: ImageView = itemView.findViewById(R.id.checkIcon)

        init {
            checkIcon.setOnClickListener {
                updateCheckColor()
                listener.onItemClick(adapterPosition)
            }

            itemView.setOnClickListener {
                editTask(adapterPosition)
            }
        }

        private fun editTask(position: Int) {
            val context = itemView.context
            val selectedTask = tasks[position]
            (context as ParentActivity).editTask(selectedTask)
        }

        private fun updateCheckColor() {
            val task = tasks[adapterPosition]
            if (!task.completed) { // currently uncompleted - to mark as done
                checkIcon.imageTintList = ColorStateList.valueOf(getColor(itemView.context, androidx.appcompat.R.attr.colorAccent))
            } else { // currently completed - to mark as undone
                checkIcon.imageTintList = ColorStateList.valueOf(getColor(itemView.context, R.attr.calendarDialogCheckColor))
            }
            checkIcon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
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
        holder.setIsRecyclable(false)
        val task = tasks[position]
        val context = holder.tvSubject.context
        holder.tvSubject.text = task.subject
        holder.tvTaskName.text = tasks[position].task

        // set color coded tab
        val bgColorIndex: Int? = mapSubjectColor[task.subject]
        val bgColor: Int = if (bgColorIndex != null) {
            ContextCompat.getColor(context, colors[bgColorIndex])
        } else {
            ContextCompat.getColor(context, R.color.gray)
        }
        holder.ivColorCode.imageTintList = ColorStateList.valueOf(bgColor)

        // set check color
        if (showCompletedTasks) {
            if (task.completed) { // set to checked if task completed
                holder.checkIcon.imageTintList = ColorStateList.valueOf(getColor(context, androidx.appcompat.R.attr.colorAccent))
            } else {
                holder.checkIcon.imageTintList = ColorStateList.valueOf(getColor(context, R.attr.calendarDialogCheckColor))
            }
        }
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
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }
}