package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.homeworklogapp.databinding.ActivityAddTaskBinding
import java.io.File
import java.io.StringReader
import java.util.*

class ActivityAddTask : AppCompatActivity() {

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

    lateinit var dueDate: String
    var dateInt = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_task)

        // datePicker stuff
        val today = Calendar.getInstance()
        val datePicker: DatePicker = findViewById(R.id.dpDueDate)

        datePicker.init(today.get(Calendar.YEAR),
        today.get(Calendar.MONTH),
        today.get(Calendar.DAY_OF_MONTH)) { view, year, month, day ->
            val month = month + 1
            dueDate = "$day $month $year"

            dateInt = createDateInt(day, month, year)
        }

        // when button "confirm" is clicked
        findViewById<Button>(R.id.btnConfirm).setOnClickListener() {

            val subject = findViewById<TextView>(R.id.etSubject).toString()
            val task = findViewById<TextView>(R.id.etTask).toString()
            val status = false // false = undone, true = done

            // stores subject, task, notes in local file
            storeLocally(subject, task, status)

            // start ActivityMainLog
            val intent = Intent(this, ActivityMainLog::class.java)
            startActivity(intent)
            finish()
        }
    }
}