package com.example.logit.calendar

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.PagerAdapter
import com.example.logit.ParentActivity
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.addtask.AddTaskActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.temporal.ChronoUnit
import java.util.*

class PagerAdapter(
    private val context: Context,
    private val mapOfTasks: Map<Int, List<Task>>,
    private val minDate: Calendar,
    private val maxDate: Calendar,
    private val selectedDate: Calendar
) : PagerAdapter() {

    private val initialPosition = ChronoUnit.DAYS.between(minDate.toInstant(), selectedDate.toInstant()).toInt()
    // number of days between minDate and selectedDate to determine ViewPager's initial position

    private val initialPageAndDate = Pair<Int, Calendar>(initialPosition, selectedDate)

    override fun getCount(): Int {
        return ChronoUnit.DAYS.between(minDate.toInstant(), maxDate.toInstant()).toInt() // total number of days between minDate and maxDate
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {

        val currentDate: Calendar = initialPageAndDate.second.clone() as Calendar
        currentDate.add(Calendar.DATE, position - initialPageAndDate.first) // adds the number of days it is away from the initialDate
        val currentDateInt = calendarToInt(currentDate)

        val view = LayoutInflater.from(context).inflate(R.layout.calendar_card_item, container, false)
        view.tag = position // tag for adjustments of size and opacity in CalendarDialog.updatePager

        val tvDayOfMonth: TextView = view.findViewById(R.id.tvDayOfMonth)
        val tvDayOfWeek: TextView = view.findViewById(R.id.tvDayOfWeek)
        val tvNoEvents: TextView = view.findViewById(R.id.tvNoEvents)
        val rvEvents: RecyclerView = view.findViewById(R.id.rvEvents)
        val fabAddTask: FloatingActionButton = view.findViewById(R.id.fabNewTask)

        tvDayOfMonth.text = currentDate.get(Calendar.DAY_OF_MONTH).toString()
        tvDayOfWeek.text = getDayOfWeek(currentDate.get(Calendar.DAY_OF_WEEK))

        // fabAddTask functionality
        if (currentDateInt < calendarToInt(Calendar.getInstance())) { // currentDate is in the past
            fabAddTask.visibility = View.GONE
        } else {
            fabAddTask.setOnClickListener { // calls method in ParentActivity since PagerAdapter has no property 'listSubjects' 
                (context as ParentActivity).createNewTask(calendarToString(currentDate))
            }
        }

        // show today's events
        var hasEvents = false
        for (key in mapOfTasks.keys) { // check if mapOfEvents contains a key with same date as currentDate
            if (key == currentDateInt) {
                val todayTasks: List<Task> = mapOfTasks[key]!!
                val adapter = RecyclerViewAdapter(todayTasks)
                rvEvents.adapter = adapter
                hasEvents = true
                break
            }
        }

        if (!hasEvents) { // no events for the day
            rvEvents.visibility = View.GONE
            tvNoEvents.visibility = View.VISIBLE
        }

        container.addView(view)
        return view
    }

    private fun getDayOfWeek(dayInt: Int): String {
        return when (dayInt) {
            2 -> "Monday"
            3 -> "Tuesday"
            4 -> "Wednesday"
            5 -> "Thursday"
            6 -> "Friday"
            7 -> "Saturday"
            else -> "Sunday"
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

    override fun getItemPosition(`object`: Any): Int {
        return POSITION_NONE // not sure what this does but it was included in the original CalendarView-Widget app by hugomfandrade
    }

    private fun calendarToInt(date: Calendar): Int {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH)+1
        val day = date.get(Calendar.DAY_OF_MONTH)

        var monthString = month.toString()
        var dayString = day.toString()

        // ensure proper MM format
        if (month < 10) {
            monthString = "0$month" // eg convert "8" to "08"
        }

        // ensure proper DD format
        if (day < 10) {
            dayString = "0$day"
        }

        // convert to YYYYMMDD format
        val dateString = "$year$monthString$dayString"
        return (dateString.toInt())
    }

    private fun calendarToString(calendar: Calendar): String {
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)+1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        var monthString = month.toString()
        var dayString = day.toString()

        // ensure proper MM format
        if (month < 10) {
            monthString = "0$month" // eg convert "8" to "08"
        }

        // ensure proper DD format
        if (day < 10) {
            dayString = "0$day"
        }

        // convert to DD MM YYYY format
        return "$dayString $monthString $year"
    }
}