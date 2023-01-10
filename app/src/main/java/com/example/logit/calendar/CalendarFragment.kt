package com.example.logit.calendar

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.logit.R
import com.example.logit.ViewModelParent
import com.example.logit.addtask.AddTaskActivity
import com.example.logit.databinding.FragmentCalendarBinding
import com.example.logit.Task
import com.example.logit.mainlog.CardColor
import kotlinx.coroutines.selects.select
import org.hugoandrade.calendarviewlib.CalendarView
import java.text.DateFormatSymbols
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment() {

    private var selectedDate: Calendar = Calendar.getInstance()

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ViewModelParent
    private lateinit var calDialogView: View
    private lateinit var viewPagerAdapter: PagerAdapter
    private lateinit var viewPager: ViewPager
    private lateinit var mAlertDialog: AlertDialog

    private lateinit var mapOfTasks: Map<Int, List<Task>>
    private lateinit var minDate: Calendar
    private lateinit var maxDate: Calendar
    private lateinit var todoList: List<Task>
    private lateinit var cardColors: List<CardColor>
    private lateinit var mapSubjectColor: Map<String, Int>

    // for PagerAdapter swipe animations
    private val MIN_OFFSET = 0f
    private val MAX_OFFSET = 0.5f
    private val MIN_ALPHA = 0.5f
    private val MIN_SCALE = 0.8f
    private var totalPages = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]
        setMinMaxDates()
        mapOfTasks = viewModel.getMapOfTodoTasks()
        todoList = viewModel.getTodoList()
        cardColors = viewModel.getListCardColors()
        mapSubjectColor = viewModel.getMapSubjectColor()
    }

    override fun onResume() {
        super.onResume()

        todoList = viewModel.getTodoList()
        cardColors = viewModel.getListCardColors()
        mapSubjectColor = viewModel.getMapSubjectColor()
        mapOfTasks = viewModel.getMapOfTodoTasks()
        setupCalendar()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
    }

    private fun setMinMaxDates() {
        // minDate and maxDate will be passed to PagerAdapter
        // so it knows how many pages there are and what page it should be showing
        minDate = Calendar.getInstance()
        minDate.set(1992, 0, 1) // note: Calendar has months from 0 - 11
        maxDate = Calendar.getInstance()
        maxDate.set(2100, 0, 1)
        totalPages = ChronoUnit.DAYS.between(minDate.toInstant(), maxDate.toInstant()).toInt()
    }

    private fun setupCalendar() {
        // initial sync
        val currentMonth = binding.calendarView.shownMonth
        val currentYear = binding.calendarView.shownYear
        syncMonth(currentMonth, currentYear)

        // sync as user swipes through calendar
        binding.calendarView.setOnMonthChangedListener { month, year ->
            syncMonth(month, year)
        }

        addCalendarObjects() // add user events to calendar
        binding.calendarView.setOnItemClickedListener { calendarObjects, previousDate, selectedDate1 ->
            if (calendarObjects.size > 0) { // if there are events
                showCalendarDialog(selectedDate1)

            } else { // if no events that day
                // user has to click twice to create new task 
                if (isSameDate(selectedDate, selectedDate1)) {
                    createNewTask()
                } else {
                    selectedDate = selectedDate1
                }
            }
        }
    }

    private fun isSameDate(date1: Calendar, date2: Calendar): Boolean {
        if (date1.get(Calendar.DAY_OF_MONTH) != date2.get(Calendar.DAY_OF_MONTH)) {
            return false
        }
        if (date1.get(Calendar.MONTH) != date2.get(Calendar.MONTH)) {
            return false
        }
        return date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR)
    }

    private fun createNewTask() {
        val intent = Intent(requireContext(), AddTaskActivity::class.java)

        val listSubjects: ArrayList<String> = viewModel.getListSubjects()
        intent.putExtra("listSubjects", listSubjects)
        startActivity(intent)
    }

    private fun addCalendarObjects() { // add CalendarObjects to CalendarView
        val calObjectList = arrayListOf<CalendarView.CalendarObject>()
        for (task in todoList) {
            val dueDate: Calendar = intToCalendar(task.dueDateInt)
            val bgColorIndex = mapSubjectColor[task.subject]
            val bgColor = if (bgColorIndex != null) {
                ContextCompat.getColor(requireContext(), cardColors[bgColorIndex].backgroundColor)
            } else {
                ContextCompat.getColor(requireContext(), R.color.gray)
            }
            calObjectList.add(
                CalendarView.CalendarObject(
                    null,
                    dueDate,
                    bgColor,
                    ContextCompat.getColor(requireContext(), com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
                )
            )
        }
        binding.calendarView.setCalendarObjectList(calObjectList)
    }

    private fun syncMonth(currentMonth: Int, currentYear: Int) {
        // a custom Month header was used in this calendar
        // this syncs the text of the custom header with the month shown in the Calendar
        val month = DateFormatSymbols().months[currentMonth]
        binding.tvMonth.text = "$month $currentYear"
    }

    private fun showCalendarDialog(selectedDate: Calendar) {
        // inflate the view for the calendar dialog
        calDialogView = View.inflate(requireContext(), R.layout.calendar_dialog, null)

        // set up the ViewPager adapter
        viewPagerAdapter = PagerAdapter(requireContext(), mapOfTasks, minDate, maxDate, selectedDate)

        val index = ChronoUnit.DAYS.between(minDate.toInstant(), selectedDate.toInstant()).toInt() // corresponding index for the current date

        viewPager = calDialogView.findViewById(R.id.viewPager)
        viewPager.apply {
            offscreenPageLimit = 3
            adapter = viewPagerAdapter
            currentItem = index
            setPadding(100, 0, 100, 0)
        }
        pagerSwipeAnimations()

        // display calDialogView in an AlertDialog
        mAlertDialog = AlertDialog.Builder(requireContext()).create()
        if (mAlertDialog.window != null) {
            mAlertDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        mAlertDialog.apply {
            setCanceledOnTouchOutside(true)
            show()
            setContentView(calDialogView)
        }
    }

    private fun pagerSwipeAnimations() {
        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                // update view scale and alpha of views not currently focused

                updatePager(
                    viewPager.findViewWithTag(position),
                    1f - positionOffset
                ) // current page
                if ((position + 1) < totalPages) { // next page
                    updatePager(viewPager.findViewWithTag(position + 1), positionOffset)
                }
                if ((position + 2) < totalPages) { // two pages in advance
                    // (so it's already made smaller before user can see it - smoother look)
                    updatePager(viewPager.findViewWithTag(position + 2), 0f)
                }
                if ((position - 1) >= 0) { // previous page
                    updatePager(viewPager.findViewWithTag(position - 1), 0f)
                }
                if ((position - 2) >= 0) { // two pages before
                    updatePager(viewPager.findViewWithTag(position - 2), 0f)
                }
            }

            override fun onPageSelected(position: Int) {
                // do nothing
            }

            override fun onPageScrollStateChanged(state: Int) {
                // do nothing
            }
        })
    }

    private fun updatePager(view: View, offset: Float) {
        // this method adjusts the size and opacity of ViewPager views which aren't currently focused
        var adjustedOffset: Float =
            (1.0f - 0.0f) * (offset - MIN_OFFSET) / (MAX_OFFSET - MIN_OFFSET) + 0.0f
        adjustedOffset = if (adjustedOffset > 1f) 1f else adjustedOffset
        adjustedOffset = if (adjustedOffset < 0f) 0f else adjustedOffset

        val alpha: Float =
            adjustedOffset * (1f - MIN_ALPHA) + MIN_ALPHA
        val scale: Float =
            adjustedOffset * (1f - MIN_SCALE) + MIN_SCALE

        view.alpha = alpha
        view.scaleY = scale
    }

    private fun intToCalendar(int: Int): Calendar {
        val string = int.toString()
        val year = string.take(4).toInt()
        val monthDay = string.takeLast(4)
        val month = (monthDay.take(2).toInt() - 1) // Calendar months go from 0 to 11
        val day = monthDay.takeLast(2).toInt()

        val calendar = Calendar.getInstance()
        calendar.set(year, month, day)
        return calendar
    }
}
