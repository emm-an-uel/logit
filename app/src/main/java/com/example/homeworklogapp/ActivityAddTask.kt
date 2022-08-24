package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.StringReader
import java.time.LocalDate
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList

class ActivityAddTask : AppCompatActivity() {

    lateinit var dueDate: String
    lateinit var listTask: ArrayList<Task>
    lateinit var currentTask: Task
    lateinit var today: Calendar
    var dateInt = 0

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        val editTaskId = intent.getStringExtra("taskId")

        if (editTaskId != null) { // if there's a task to be edited
            // todo: fix crash when task is edited
            
            currentTask = findCurrentTask(editTaskId)

            // datePicker stuff
            val dateList = currentTask.dueDate.split(" ").toList()

            val dueYear = dateList[2].toInt()
            val dueMonth = dateList[1].toInt() - 1
            val dueDay = dateList[0].toInt()

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
            findViewById<EditText>(R.id.etTask).text = currentTask.task.toEditable()
            findViewById<EditText>(R.id.etSubject).text = currentTask.subject.toEditable()

        } else { // if there's no task to be edited
            today = Calendar.getInstance()
            val datePicker: DatePicker = findViewById(R.id.dpDueDate)

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

            // todo: if any of the fields aren't filled, raise error
            // todo: trim - remove end space

            val subject = findViewById<EditText>(R.id.etSubject).text.toString()
            val task = findViewById<EditText>(R.id.etTask).text.toString()
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

        // create val "assignment" using Class "Assignment" parameters
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