package com.example.homeworklogapp

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworklogapp.databinding.DateItemBinding
import com.example.homeworklogapp.databinding.GeneralItemBinding
import java.util.*

class RVAdapterMain (
    private val listOfItems: List<ListItem>,
    private val mapSubjectColor: HashMap<String, Int>,
    private val listColors: ArrayList<CardColor>,
    private val glow: Boolean
        ): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            ListItem.TYPE_DATE ->
                DateViewHolder(DateItemBinding.inflate(layoutInflater, parent, false))
            else ->
                GeneralViewHolder(GeneralItemBinding.inflate(layoutInflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            ListItem.TYPE_DATE -> {
                (holder as DateViewHolder).bind(
                    item = listOfItems[position] as DateItem
                )
            }
            ListItem.TYPE_TASK -> {
                (holder as GeneralViewHolder).bind(
                    item = listOfItems[position] as TaskItem
                )
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return listOfItems[position].type
    }

    override fun getItemCount(): Int {
        return listOfItems.size
    }

    inner class DateViewHolder(val binding: DateItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DateItem) {
            // convert to format 'DD MM YYYY'
            val year = item.date.take(4)
            val monthDay = item.date.takeLast(4)
            val month = monthDay.take(2)
            val day = monthDay.takeLast(2)

            val actualDate = "$day $month $year"
            binding.tvDate.text = actualDate
        }
    }

    inner class GeneralViewHolder(val binding: GeneralItemBinding): RecyclerView.ViewHolder(binding.root) {
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

        return "Error"
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

        return "Error"
    }
}