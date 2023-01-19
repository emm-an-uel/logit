package com.example.logit.log

import android.content.Context
import android.content.res.ColorStateList
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.databinding.HeaderItemBinding
import com.example.logit.databinding.TaskItemBinding
import java.util.*

class RVAdapterLog (
    private var listOfItems: List<ListItem>,
    private val mapSubjectColor: HashMap<String, Int>,
    private val listColors: ArrayList<CardColor>,
    private val glow: Boolean,
    private val bars: Boolean
        ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ListItem.TYPE_HEADER ->
                DateViewHolder(HeaderItemBinding.inflate(layoutInflater, parent, false))
            else ->
                TaskViewHolder(TaskItemBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ListItem.TYPE_HEADER -> {
                (holder as DateViewHolder).bind(
                    item = listOfItems[position] as HeaderItem
                )
            }
            ListItem.TYPE_TASK -> {
                holder.setIsRecyclable(false)
                (holder as TaskViewHolder).bind(
                    item = listOfItems[position] as TaskItem
                )
            }
        }
    }

    // filtering results
    fun filterList(filteredList: ArrayList<ListItem>) {
        listOfItems = filteredList
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return listOfItems[position].type
    }

    override fun getItemCount(): Int {
        return listOfItems.size
    }

    inner class DateViewHolder(val binding: HeaderItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HeaderItem) {
            binding.tvHeader.text = item.header

            // bar color for emphasis on overdue and due today
            val context = binding.ivBar.context
            when (item.header) {
                "Overdue" -> {
                    val barColor = ContextCompat.getColor(context, R.color.red)
                    binding.tvHeader.setTextColor(barColor)
                    if (bars) { // only run the following code if user wants header bars
                        binding.ivBar.apply {
                            visibility = View.VISIBLE
                            imageTintList = ColorStateList.valueOf(barColor)
                        }
                    }
                }
                "Due Today" -> {
                    val barColor = ContextCompat.getColor(context, R.color.orange)
                    binding.tvHeader.setTextColor(barColor)
                    if (bars) {
                        binding.ivBar.apply {
                            visibility = View.VISIBLE
                            imageTintList = ColorStateList.valueOf(barColor)
                        }
                    }
                }
                else -> {
                    if (bars) {
                        val barColor = binding.tvHeader.currentTextColor
                        binding.ivBar.apply {
                            visibility = View.VISIBLE
                            imageTintList = ColorStateList.valueOf(barColor)
                        }
                    }
                }
            }
        }
    }

    inner class TaskViewHolder(val binding: TaskItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskItem) {
            val context = binding.cardView.context

            // setup background color
            val backgroundThemeColor = getColor(context, com.google.android.material.R.attr.colorContainer)
            binding.linearLayoutBackground.setBackgroundColor(backgroundThemeColor)

            // setup textviews
            binding.tvSubject.text = item.subject
            binding.tvTask.text = item.task

            val currentDueDateList = item.dueDate.split(" ")
            val dayOfMonth = currentDueDateList[0].toInt()
            val month = currentDueDateList[1].toInt() - 1
            val year = currentDueDateList[2].toInt()
            val c = Calendar.getInstance()
            c.set(year, month, dayOfMonth)
            binding.tvDueDate.text = getDueDateText(c)

            if (item.notes != "") {
                binding.tvNotes.text = item.notes
                binding.tvNotes.visibility = View.VISIBLE
            }

            // set tab colors
            val bgColorIndex = mapSubjectColor[item.subject]
            val textColor = getColor(context, com.google.android.material.R.attr.colorOnSecondary)

            if (bgColorIndex != null) {
                val bgColor = ContextCompat.getColor(context, listColors[bgColorIndex].backgroundColor)
                binding.cardView.setCardBackgroundColor(bgColor)
                if (glow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    binding.cardView.outlineSpotShadowColor = bgColor
                }
            } else {
                val bgColor = ContextCompat.getColor(context, R.color.gray)
                binding.cardView.setCardBackgroundColor(bgColor)
                if (glow && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    binding.cardView.outlineSpotShadowColor = bgColor
                }
            }

            binding.tvSubject.setTextColor(textColor)
            binding.tvTask.setTextColor(textColor)
            binding.tvDueDate.setTextColor(textColor)

            if (binding.tvNotes.visibility == View.VISIBLE) {
                binding.tvNotes.apply {
                    setTextColor(textColor)
                    alpha = 0.65F
                }
            }

            // click listener
            itemView.setOnClickListener {
                mListener.onItemClick(adapterPosition)
            }
        }
    }

    // click listener
    private lateinit var mListener: OnItemClickListener

    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun getDueDateText(c: Calendar): String {
        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val actualDayOfWeek = getActualDayOfWeek(dayOfWeek)

        val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)

        val month1 = c.get(Calendar.MONTH) + 1
        val actualMonth = getActualMonth(month1)

        val year = c.get(Calendar.YEAR).toString().takeLast(2) // last two numbers (eg "2022" becomes "22")

        return "$actualDayOfWeek, $dayOfMonth $actualMonth $year"
    }

    private fun getActualDayOfWeek(dayOfWeek: Int): String {
        when (dayOfWeek) {
            1 -> return "Sun"
            2 -> return "Mon"
            3 -> return "Tue"
            4 -> return "Wed"
            5 -> return "Thu"
            6 -> return "Fri"
            7 -> return "Sat"
        }
        return "Unknown Day"
    }

    private fun getActualMonth(month: Int): String {
        when (month) {
            1 -> return "Jan"
            2 -> return "Feb"
            3 -> return "Mar"
            4 -> return "Apr"
            5 -> return "May"
            6 -> return "Jun"
            7 -> return "Jul"
            8 -> return "Aug"
            9 -> return "Sep"
            10 -> return "Oct"
            11 -> return "Nov"
            12 -> return "Dec"
        }
        return "Unknown Month"
    }
}