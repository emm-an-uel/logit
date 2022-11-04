package com.example.homeworklogapp

import android.app.Application
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

    // TODO: get() methods might cause crashes if lists are empty?

    @JvmName("getTodoList1")
    fun getTodoList(): ArrayList<Task> {
        return todoList
    }

    @JvmName("getDoneList1")
    fun getDoneList(): ArrayList<Task> {
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