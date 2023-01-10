package com.example.logit.addtask

import android.content.Context
import android.os.Bundle
import android.widget.CalendarView
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.logit.R
import com.example.logit.Task
import java.io.File
import java.io.StringReader
import java.util.*

class AddTaskActivity : AppCompatActivity() {

    lateinit var dueDate: String
    lateinit var listTask: ArrayList<Task>
    lateinit var currentTask: Task
    var dateInt = 0

    lateinit var spinnerSubject: Spinner
    lateinit var etTask: EditText
    lateinit var cvDueDate: CalendarView
    lateinit var etNotes: EditText

    lateinit var tvConfirm: TextView

    var taskIsBeingEdited = true

    lateinit var listSubjectsSpinner: ArrayList<String>

    lateinit var rvAddTask: RecyclerView

    var btnIsClickable = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        var editTaskId = intent.getStringExtra("taskId")
        if (editTaskId == null) { // if user is creating a new task
            title = "Create New Task" // this sets the activity's label
            editTaskId = "0"
            taskIsBeingEdited = false
        } else { // if user is editing an existing task
            title = "Edit a Task"
            currentTask = findCurrentTask(editTaskId)
        }

        setupRVAddTask(editTaskId)

        // set btnConfirm to unclickable by default
        tvConfirm = findViewById(R.id.tvConfirm)
        btnDisabled()

        // when button "confirm" is clicked
        tvConfirm.setOnClickListener() {

            if (btnIsClickable) {

                assignViews()

                val selectedSpinnerIndex = spinnerSubject.selectedItemPosition
                val subject = listSubjectsSpinner[selectedSpinnerIndex]

                val task = etTask.text.toString().trim()
                val status = false // false = undone, true = done

                val notes = etNotes.text.toString().trim()

                // stores subject, task, notes in local file
                storeLocally(subject, task, status, notes)

                finish()
            }
        }
    }

    private fun setupRVAddTask(editTaskId: String) {
        // initialize litSubjectsSpinner
        listSubjectsSpinner = arrayListOf()

        listSubjectsSpinner.addAll(intent.getStringArrayListExtra("listSubjects")!!) // listSubjects: ArrayList<String>
        listSubjectsSpinner.add("Other") // final option of "Other"

        val selectedDateString: String? = intent.getStringExtra("selectedDate")

        // initialize rv
        rvAddTask = findViewById(R.id.rvAddTask)
        val rvAdapter = RVAdapterAddTask(listSubjectsSpinner, editTaskId, selectedDateString)
        rvAddTask.adapter = rvAdapter
    }

    private fun assignViews() {
        val itemCount = rvAddTask.adapter!!.itemCount

        for (i in 0 until itemCount) {

            val holder = rvAddTask.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                when (i) {
                    0 -> { // subject
                        spinnerSubject = holder.itemView.findViewById(R.id.spinnerSubject)
                    }

                    1 -> { // task
                        etTask = holder.itemView.findViewById(R.id.etTask)
                    }

                    2 -> { // due date
                        cvDueDate = holder.itemView.findViewById(R.id.cvDueDate)
                    }

                    else -> { // notes
                        etNotes = holder.itemView.findViewById(R.id.etNotes)
                    }
                }
            }
        }
    }

    fun updateDueDate(date: String) {
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
        tvConfirm.alpha = 0.2F
    }

    fun btnEnabled() {
        btnIsClickable = true
        tvConfirm.alpha = 1F
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

    private fun storeLocally(subject : String, task : String, status: Boolean, notes: String) {
        val id = UUID.randomUUID().toString()

        // create val "assignment" using Class "Task" parameters
        val newAssignment = Task(id, subject, task, dueDate, dateInt, status, notes, 0)

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
}