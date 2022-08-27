package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.StringReader
import java.util.*

class ActivityAddTask : AppCompatActivity() {

    lateinit var dueDate: String
    lateinit var listTask: ArrayList<Task>
    lateinit var currentTask: Task
    lateinit var today: Calendar
    var dateInt = 0

    lateinit var etTask: EditText
    lateinit var etSubject: EditText

    var editedTask = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        etTask = findViewById(R.id.etTask)
        etSubject = findViewById(R.id.etSubject)

        val editTaskId = intent.getStringExtra("taskId")

        if (editTaskId != null) { // if there's a task to be edited

            editedTask = true

            currentTask = findCurrentTask(editTaskId)

            // datePicker stuff
            val dateList = currentTask.dueDate.split(" ").toList()

            val dueYear = dateList[2].toInt()
            val dueMonth = dateList[1].toInt() - 1
            val dueDay = dateList[0].toInt()

            val dueMonthCurrent = dateList[1].toInt() // dueMonth is month - 1, which satisfies DatePicker but is not the actual month
            dueDate = "$dueDay $dueMonthCurrent $dueYear"
            dateInt = createDateInt(dueDay, dueMonthCurrent, dueYear)

            today = Calendar.getInstance()
            today.set(dueYear, dueMonth, dueDay) // convert to dueDate if there's a task being edited

            val datePicker: DatePicker = findViewById(R.id.dpDueDate)
            datePicker.init(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
                val month = month + 1

                dueDate = "$day $month $year"
                dateInt = createDateInt(day, month, year)
            }

            // set EditTexts' content appropriately
            etTask.text = currentTask.task.toEditable()
            etSubject.text = currentTask.subject.toEditable()

        } else { // if there's no task to be edited
            today = Calendar.getInstance()
            val datePicker: DatePicker = findViewById(R.id.dpDueDate)

            // default due date is today's date
            val todayDay = today.get(Calendar.DAY_OF_MONTH)
            val todayMonth = today.get(Calendar.MONTH) + 1
            val todayYear = today.get(Calendar.YEAR)
            dueDate = "$todayDay $todayMonth $todayYear"
            dateInt = createDateInt(todayDay, todayMonth, todayYear)

            datePicker.init(today.get(Calendar.YEAR),
                today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
                val month = month + 1

                dueDate = "$day $month $year"
                dateInt = createDateInt(day, month, year)
            }
        }

        // when button "confirm" is clicked
        findViewById<Button>(R.id.btnConfirm).setOnClickListener() {

            // todo: show confirm button only if task is filled 

            val subject = if (etSubject.text.toString() == "") { // subject = "Other" if not filled by user
                "Other"
            } else {
                etSubject.text.toString().trim()
            }

            val task = etTask.text.toString().trim()
            val status = false // false = undone, true = done

            // stores subject, task, notes in local file
            storeLocally(subject, task, status)

            // start ActivityMainLog
            val intent = Intent(this, ActivityMainLog::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    private fun findCurrentTask(editTaskId: String): Task {
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

    private fun storeLocally(subject : String, task : String, status: Boolean) {
        val id = UUID.randomUUID().toString()

        // create val "assignment" using Class "Task" parameters
        val newAssignment = Task(id, subject, task, dueDate, dateInt, status)

        // check if there's existing "fileAssignments"
        var fileExists = false
        val files = this.fileList()
        for (file in files) {
            if (file == "fileAssignment") {
                fileExists = true
                break
            }
        }

        if (fileExists) { // if there's existing "fileAssignments"

            val listAssignments = arrayListOf<Task>()

            // read json
            val fileJson = File(this.filesDir, "fileAssignment").readText()

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
            if (editedTask) { // if there was a task which got edited
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