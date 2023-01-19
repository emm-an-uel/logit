package com.example.logit.addtask

import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.databinding.ActivityAddTaskBinding
import java.io.File
import java.io.StringReader
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    private lateinit var spinnerSubject: Spinner
    private lateinit var etTask: EditText
    private lateinit var tvDueDate: TextView
    private lateinit var cvDueDate: CalendarView
    private lateinit var etNotes: EditText

    private lateinit var dueDate: String
    private lateinit var listTask: ArrayList<Task>
    private lateinit var currentTask: Task
    private var dateInt = 0

    private var taskIsBeingEdited = true
    private var btnIsClickable = false
    private var showCvDueDate = false
    private var originalSpinnerIndex = 0

    private lateinit var listSubjectsSpinner: ArrayList<String>

    private lateinit var binding: ActivityAddTaskBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTaskBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val editTaskId = intent.getStringExtra("taskId")
        if (editTaskId == null) { // if user is creating a new task
            title = "Create New Task" // this sets the activity's label
            taskIsBeingEdited = false
        } else { // if user is editing an existing task
            title = "Edit a Task"
            currentTask = findCurrentTask(editTaskId)
        }

        setupViews()

        // set btnConfirm to unclickable by default
        btnDisabled()

        // when button "confirm" is clicked
        binding.btnConfirm.setOnClickListener {

            if (btnIsClickable) {
                val selectedSpinnerIndex = spinnerSubject.selectedItemPosition
                val subject = listSubjectsSpinner[selectedSpinnerIndex]
                val task = etTask.text.toString().trim()
                val completed = false
                val notes = etNotes.text.toString().trim()

                storeLocally(subject, task, completed, notes)
                finish()
            }
        }
    }

    private fun setupViews() {
        // initialize listSubjectsSpinner
        listSubjectsSpinner = intent.getStringArrayListExtra("listSubjects")!!
        listSubjectsSpinner.add("Other") // final option of "Other"

        // if user created a new task from Calendar
        val selectedDateString: String? = intent.getStringExtra("selectedDate")

        // set views
        spinnerSubject = binding.content.spinnerSubject
        etTask = binding.content.etTask
        tvDueDate = binding.content.tvDueDate
        cvDueDate = binding.content.cvDueDate
        etNotes = binding.content.etNotes

        // subject
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, listSubjectsSpinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSubject.adapter = adapter
        if (taskIsBeingEdited) {
            setSubjectInfo(currentTask.subject)
            spinnerSubjectItemSelectedListener()
        } else {
            setSubjectInfo("Other")
        }

        // task
        if (taskIsBeingEdited) {
            setTaskInfo(currentTask.task)
            etTask.addTextChangedListener(textWatcherTask)
        } else {
            etTask.addTextChangedListener(textWatcherNewTask)
        }

        // due date
        val primaryTextColor = getColor(this, R.attr.primaryTextColor)
        setMinDate()
        if (taskIsBeingEdited) {
            setDefaultDueDate(currentTask.dueDate)
        } else if (selectedDateString != null) { // would only be non null if user created new task from Calendar
            setDefaultDueDate(selectedDateString)
        } else {
            setDefaultDueDate(null)
        }
        tvDueDate.setOnClickListener {
            if (!showCvDueDate) { // currently not shown -> shown
                cvDueDate.visibility = View.VISIBLE
                tvDueDate.setTextColor(ContextCompat.getColor(this, R.color.cardview_black))

                // set tvDueDate bg color
                val bgColor = getColor(this, com.google.android.material.R.attr.colorSecondaryContainer)
                tvDueDate.backgroundTintList = ColorStateList.valueOf(bgColor)
                showCvDueDate = true

            } else { // currently shown -> not shown
                cvDueDate.visibility = View.GONE
                tvDueDate.setTextColor(primaryTextColor)

                val bgColor = getColor(this, com.google.android.material.R.color.mtrl_btn_transparent_bg_color)
                tvDueDate.backgroundTintList = ColorStateList.valueOf(bgColor)
                showCvDueDate = false
            }
        }

        cvDueDate.visibility = View.GONE // gone by default
        cvDueDate.setOnDateChangeListener { _, year, month, dayOfMonth ->
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
                    btnDisabled()
                } else {
                    btnEnabled()
                }
            }

            updateDueDate(dueDate)
        }

        // notes
        if (taskIsBeingEdited) {
            setNotesInfo(currentTask.notes)
            etNotes.addTextChangedListener(textWatcherNotes)
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
            updateDueDate(defaultDueDate)

        } else {

            // sets default due date a today's date, passes dueDate to ActivityAddTask - this is so dueDate != null if user doesn't touch tvDueDate
            val day = c.get(Calendar.DAY_OF_MONTH)
            val month = c.get(Calendar.MONTH) + 1
            val year = c.get(Calendar.YEAR)

            val defaultDueDate = "$day $month $year"
            updateDueDate(defaultDueDate)
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

    private fun updateDueDate(date: String) {
        dueDate = date
        val dueDateList = dueDate.split(" ")
        val day = dueDateList[0].toInt()
        val month = dueDateList[1].toInt()
        val year = dueDateList[2].toInt()

        dateInt = createDateInt(day, month, year)
    }

    private fun createDateInt(day: Int, month: Int, year: Int): Int {
        // * dueDateSort will be in format YYYYMMDD for easy sorting of due dates *

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
        val dateInt = dateString.toInt() // return integer so it can be sorted

        return(dateInt)
    }


    fun btnDisabled() {
        btnIsClickable = false
        binding.btnConfirm.alpha = 0.2F
    }

    fun btnEnabled() {
        btnIsClickable = true
        binding.btnConfirm.alpha = 1F
    }

    fun findCurrentTask(editTaskId: String): Task {
        listTask = ArrayList()

        val file = File(this.filesDir, "fileAssignment")
        lateinit var currentTask: Task

        // * deserialize and read .json *
        // read json file
        val fileJson = file.readText()

        // convert fileJson into listTask: List
        JsonReader(StringReader(fileJson)).use { reader ->
            reader.beginArray {
                while (reader.hasNext()) {
                    val t = Klaxon().parse<Task>(reader)
                    listTask.add(t!!)
                }
            }
        }

        for (task in listTask) {
            if (task.id == editTaskId) {
                currentTask = task
                break
            }
        }

        return currentTask
    }

    private fun storeLocally(subject : String, task : String, completed: Boolean, notes: String) {
        val id = UUID.randomUUID().toString()

        // create val "assignment" using Class "Task" parameters
        val newAssignment = Task(id, subject, task, dueDate, dateInt, completed, notes, 0)

        // check if there's existing "fileAssignments"
        val file = File(this.filesDir, "fileAssignment")

        if (file.exists()) { // if there's existing "fileAssignment"

            val listAssignments = arrayListOf<Task>()

            // read json
            val fileJson = file.readText()

            // add all items in fileJson into listAssignments
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val assignment = Klaxon().parse<Task>(reader)
                        listAssignments.add(assignment!!)
                    }
                }
            }

            // delete original task (if applicable)
            if (taskIsBeingEdited) { // if there was a task which got edited
                for (task in listAssignments) {
                    if (task.id == currentTask.id) {
                        listAssignments.remove(task)
                        break
                    }
                }
            }

            // add newAssignment and serialize listAssignments
            listAssignments.add(newAssignment)
            val updatedFile = Klaxon().toJsonString(listAssignments)

            // store in local file
            this.openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
                it.write(updatedFile.toByteArray())
            }

        } else { // if "fileAssignment" does not exist

            // new val listAssignment, add newAssignment and serialize listAssignments
            val listAssignment = mutableListOf(newAssignment)
            val updatedFile = Klaxon().toJsonString(listAssignment)

            // store in local file
            this.openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
                it.write(updatedFile.toByteArray())
            }
        }
    }

    private fun spinnerSubjectItemSelectedListener() {
        spinnerSubject.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 == originalSpinnerIndex) { // note: p2 is current position
                    btnDisabled()
                } else {
                    btnEnabled()
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
                btnDisabled()
            } else if (etTask.text.toString().trim() == "") { // if task is empty
                btnDisabled()
            } else {
                btnEnabled()
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
                btnDisabled()
            } else {
                btnEnabled()
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
                btnEnabled()
            } else {
                btnDisabled()
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