package com.example.logit.addtask

import android.content.Context
import android.content.res.ColorStateList
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.Task
import java.util.*

class RVAdapterAddTask(
    private val listSubjectsSpinner: ArrayList<String>,
    private val editTaskId: String,
    private val selectedDateString: String?
) : RecyclerView.Adapter<RVAdapterAddTask.NewViewHolder>() {

    lateinit var spinnerSubject: Spinner
    lateinit var etTask: EditText
    lateinit var tvDueDate: TextView
    lateinit var cvDueDate: CalendarView
    lateinit var etNotes: EditText

    lateinit var context: Context

    lateinit var dueDate: String

    lateinit var currentTask: Task

    private val listAddTaskRVAdapter: ArrayList<String> = arrayListOf("Subject", "Task", "Due Date", "Notes")

    private var originalSpinnerIndex = 0

    var taskIsBeingEdited = true

    var showCvDueDate = false

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewViewHolder { // inflate the layout for add_task_rv_item.xml
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_item_add_task,
            parent, false
        )

        return NewViewHolder(itemView)
    }

    class NewViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) { // initialize views
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        val tvItemName: TextView = itemView.findViewById(R.id.tvItemName)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views

        context = holder.linearLayout.context

        holder.tvItemName.text = listAddTaskRVAdapter[position] // sets tvItemName.text to "subject" or "task" etc.

        if (editTaskId != "0") { // if user is editing an existing task
            // find currentTask and set corresponding variables
            currentTask = (context as AddTaskActivity).findCurrentTask(editTaskId)
        } else { // if user is creating a new task
            taskIsBeingEdited = false
        }

        createViews(position, holder)
    }

    private fun createViews(
        position: Int,
        holder: NewViewHolder
    ) {

        // note: there's a bug with CalendarView that prevents it from being VISIBLE if initially set to GONE
        // workaround: set cvDueDate to VISIBLE in xml, but set to GONE by default here
        holder.itemView.findViewById<CalendarView>(R.id.cvDueDate).visibility = View.GONE

        when (position) {
            0 -> { // subject
                spinnerSubject = holder.itemView.findViewById(R.id.spinnerSubject)
                spinnerSubject.visibility = View.VISIBLE

                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, listSubjectsSpinner)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

                spinnerSubject.adapter = adapter

                if (taskIsBeingEdited) {
                    setSubjectInfo(currentTask.subject)
                    spinnerSubjectItemSelectedListener()
                } else {
                    setSubjectInfo("Other")
                }
            }

            1 -> { // task
                etTask = holder.itemView.findViewById(R.id.etTask)
                etTask.visibility = View.VISIBLE

                if (taskIsBeingEdited) {
                    setTaskInfo(currentTask.task)
                    etTask.addTextChangedListener(textWatcherTask)
                } else {
                    etTask.addTextChangedListener(textWatcherNewTask)
                }
            }

            2 -> { // due date

                cvDueDate = holder.itemView.findViewById(R.id.cvDueDate) // initialized here because setTvDueDateText refers to cvDueDate; so it doesn't call on a null item
                setMinDate()

                tvDueDate = holder.itemView.findViewById(R.id.tvDueDate)
                if (taskIsBeingEdited) {
                    setDefaultDueDate(currentTask.dueDate)
                } else if (selectedDateString != null) { // would only be non null if user created new task from Calendar
                    setDefaultDueDate(selectedDateString)
                } else {
                    setDefaultDueDate(null)
                }
                tvDueDate.visibility = View.VISIBLE

                val textColor = getColor(context, com.google.android.material.R.attr.colorOnSecondary)
                tvDueDate.setTextColor(textColor)

                tvDueDate.setOnClickListener {

                    if (!showCvDueDate) { // currently not shown -> shown
                        cvDueDate.visibility = View.VISIBLE
                        tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.cardview_black))

                        // set tvDueDate bg color
                        val bgColor = getColor(context, com.google.android.material.R.attr.colorSecondaryContainer)
                        tvDueDate.backgroundTintList = ColorStateList.valueOf(bgColor)
                        showCvDueDate = true

                    } else { // currently shown -> not shown
                        cvDueDate.visibility = View.GONE
                        tvDueDate.setTextColor(textColor)

                        val bgColor = getColor(context, com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
                        tvDueDate.backgroundTintList = ColorStateList.valueOf(bgColor)
                        showCvDueDate = false
                    }
                }

                cvDueDate.setOnDateChangeListener(
                    CalendarView.OnDateChangeListener { view, year, month, dayOfMonth ->
                        val month1 = month + 1
                        dueDate = "$dayOfMonth $month1 $year"

                        // displays current date on tvDueDate
                        val c = Calendar.getInstance()
                        c.set(year, month, dayOfMonth)
                        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
                        val actualDayOfWeek = getActualDayOfWeek(dayOfWeek)
                        val actualMonth = getActualMonth(month1)

                        tvDueDate.text = "$actualDayOfWeek, $dayOfMonth $actualMonth $year"

                        // btnConfirm clickability
                        if (taskIsBeingEdited) {
                            if (currentTask.dueDate == dueDate) { // if existing dueDate == new dueDate
                                (context as AddTaskActivity).btnDisabled()
                            } else {
                                (context as AddTaskActivity).btnEnabled()
                            }
                        }

                        (context as AddTaskActivity).updateDueDate(dueDate)
                    }
                )
            }

            else -> { // notes
                etNotes = holder.itemView.findViewById(R.id.etNotes)
                etNotes.visibility = View.VISIBLE

                if (taskIsBeingEdited) {
                    setNotesInfo(currentTask.notes)
                    etNotes.addTextChangedListener(textWatcherNotes)
                }
            }
        }
    }

    private fun setDefaultDueDate(currentDueDate: String?) {

        val c = Calendar.getInstance()

        if (currentDueDate != null) { // user is editing an existing task
            val currentDueDateList = currentDueDate.split(" ")
            val dayOfMonth = currentDueDateList[0].toInt()
            val month = currentDueDateList[1].toInt() - 1
            val year = currentDueDateList[2].toInt()

            c.set(year, month, dayOfMonth)

            // set cvDueDate's selected date
            val dateLong = c.timeInMillis
            cvDueDate.date = dateLong

            val actualMonth = month + 1 // refer to line 200 - month is actualMonth - 1; this adds the 1 back
            val defaultDueDate = "$dayOfMonth $actualMonth $year"
            (context as AddTaskActivity).updateDueDate(defaultDueDate)

        } else {

            // sets default due date a today's date, passes dueDate to ActivityAddTask - this is so dueDate != null if user doesn't touch tvDueDate
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month = c.get(Calendar.MONTH) + 1
            val year = c.get(Calendar.YEAR)

            val defaultDueDate = "$day $month $year"
            (context as AddTaskActivity).updateDueDate(defaultDueDate)
        }

        val dayOfWeek = c.get(Calendar.DAY_OF_WEEK)
        val actualDayOfWeek = getActualDayOfWeek(dayOfWeek)

        val dayOfMonth = c.get(Calendar.DAY_OF_MONTH)

        val month1 = c.get(Calendar.MONTH) + 1
        val actualMonth = getActualMonth(month1)

        val year = c.get(Calendar.YEAR)

        tvDueDate.text = "$actualDayOfWeek, $dayOfMonth $actualMonth $year"
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

    private fun setMinDate() {
        val today = Calendar.getInstance()
        val todayLong = today.timeInMillis
        cvDueDate.minDate = todayLong
    }

    override fun getItemCount(): Int {
        return listAddTaskRVAdapter.size
    }

    private fun spinnerSubjectItemSelectedListener() {
        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == originalSpinnerIndex) { // note: p2 is current position
                    (context as AddTaskActivity).btnDisabled()
                } else {
                    (context as AddTaskActivity).btnEnabled()
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }

        }
    }

    private val textWatcherTask = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etTask.text.toString().trim() == currentTask.task) { // if task is unchanged
                (context as AddTaskActivity).btnDisabled()
            } else if (etTask.text.toString().trim() == "") { // if task is empty
                (context as AddTaskActivity).btnDisabled()
            } else {
                (context as AddTaskActivity).btnEnabled()
            }
        }
    }

    private val textWatcherNotes = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etNotes.text.toString().trim() == currentTask.notes) { // if notes is unchanged
                (context as AddTaskActivity).btnDisabled()
            } else {
                (context as AddTaskActivity).btnEnabled()
            }
        }
    }

    private fun setSubjectInfo(subject: String) {
        originalSpinnerIndex = listSubjectsSpinner.indexOf(subject)
        spinnerSubject.setSelection(originalSpinnerIndex)
    }

    private fun setTaskInfo(task: String) {
        etTask.setText(task)
    }

    private fun setNotesInfo(notes: String) {
        etNotes.setText(notes)
    }

    private val textWatcherNewTask = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
        }
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (etTask.text.toString().trim() != "") {
                (context as AddTaskActivity).btnEnabled()
            } else {
                (context as AddTaskActivity).btnDisabled()
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

}
