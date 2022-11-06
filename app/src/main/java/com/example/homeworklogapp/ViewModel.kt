package com.example.homeworklogapp

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import java.io.File
import java.io.StringReader

class ViewModel(val app: Application): AndroidViewModel(app) {

    lateinit var todoList: ArrayList<Task>
    lateinit var doneList: ArrayList<Task>

    lateinit var listSubjectColor: ArrayList<SubjectColor>
    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listCardColors: ArrayList<CardColor>
    lateinit var listColors: ArrayList<Int>

    fun initTaskLists() {
        todoList = arrayListOf()
        doneList = arrayListOf()

        val file = File(app.filesDir, "fileAssignment")

        if (file.exists()) {

            // deserialize and read json
            val fileJson = file.readText() // read file

            // convert fileJson into list
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val task = Klaxon().parse<Task>(reader)

                        if (!task!!.status) { // undone
                            todoList.add(task)
                        } else { // done
                            doneList.add(task)
                        }
                    }
                }
            }
        }
    }

    fun taskCompleted(completedTask: Task) { // moves completedTask from todoList to doneList
        for (task in todoList) {
            if (completedTask.id == task.id) {
                todoList.remove(task)
                break
            }
        }

        completedTask.status = true // set to 'done'
        doneList.add(completedTask)

        saveJsonTaskLists()
    }

    fun restoreTask(restoredTask: Task) { // moves restoredTask from doneList to todoList
        for (task in doneList) {
            if (task.id == restoredTask.id) {
                doneList.remove(task)
                break
            }
        }

        restoredTask.status = false // set to 'undone'
        todoList.add(restoredTask)

        saveJsonTaskLists()
    }

    fun deleteTask(deletedTask: Task) { // removes deletedTask from doneList
        for (task in doneList) {
            if (task.id == deletedTask.id) {
                doneList.remove(task)
                break
            }
        }

        saveJsonTaskLists()
    }

    fun clearDoneList() { // clears all items in doneList
        doneList = arrayListOf() // sets doneList to an empty list

        saveJsonTaskLists()
    }

    private fun saveJsonTaskLists() {
        val listAllTasks = arrayListOf<Task>()
        listAllTasks.addAll(todoList)
        listAllTasks.addAll(doneList)

        val file = Klaxon().toJsonString(listAllTasks)

        app.openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
            it.write(file.toByteArray())
        }
    }

    @JvmName("getTodoList1")
    fun getTodoList(): ArrayList<Task> {
        todoList.sortBy { it.dateInt }
        return todoList
    }

    @JvmName("getDoneList1")
    fun getDoneList(): ArrayList<Task> {
        doneList.sortBy { it.dateInt }
        return doneList
    }

    fun initSubjectColor() {
        listSubjectColor = arrayListOf()
        mapSubjectColor = hashMapOf()

        val file = File(app.filesDir, "listSubjectColor")

        if (file.exists()) {

            val fileJson = file.readText()

            // convert into map
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val subjectColor = Klaxon().parse<SubjectColor>(reader)

                        listSubjectColor.add(subjectColor!!)

                        val subject = subjectColor.subject
                        val color = subjectColor.colorIndex

                        mapSubjectColor.put(subject, color)
                    }
                }
            }
        }
    }

    @JvmName("getListSubjectColor1")
    fun getListSubjectColor(): ArrayList<SubjectColor> {
        return listSubjectColor
    }

    @JvmName("getMapSubjectColor1")
    fun getMapSubjectColor(): HashMap<String, Int> {
        return mapSubjectColor
    }

    fun initListCardColors() {
        listCardColors = arrayListOf()
        listColors = arrayListOf()

        val file = File(app.filesDir, "listCardColors")

        if (file.exists()) {

            // deserialize and read json
            val fileJson = file.readText() // read file

            // convert fileJson into list
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val cardColor = Klaxon().parse<CardColor>(reader)
                        val backgroundColor = cardColor!!.backgroundColor

                        listCardColors.add(cardColor)
                        listColors.add(backgroundColor)
                    }
                }
            }
        }
        if (listCardColors.size == 0) { // if list is empty

            listCardColors = arrayListOf(
                CardColor(R.color.cardview_blue, R.color.white),
                CardColor(R.color.cardview_red, R.color.white),
                CardColor(R.color.cardview_yellow, R.color.black),
                CardColor(R.color.cardview_green, R.color.white),
                CardColor(R.color.cardview_purple, R.color.white)
            )

            listColors = arrayListOf(
                R.color.cardview_blue,
                R.color.cardview_red,
                R.color.cardview_yellow,
                R.color.cardview_green,
                R.color.cardview_purple
            )

            // save listCardColors
            val fileListCardColors = Klaxon().toJsonString(listCardColors)

            app.openFileOutput("fileListCardColors", Context.MODE_PRIVATE).use {
                it.write(fileListCardColors.toByteArray())
            }
        }
    }

    @JvmName("getListCardColors1")
    fun getListCardColors(): ArrayList<CardColor> {
        return listCardColors
    }

    @JvmName("getListColors1")
    fun getListColors(): ArrayList<Int> {
        return listColors
    }
}