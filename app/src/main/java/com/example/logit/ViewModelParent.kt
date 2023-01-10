package com.example.logit

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.logit.mainlog.*
import com.example.logit.settings.SettingsItem
import java.io.File
import java.io.StringReader
import java.util.*

class ViewModelParent(val app: Application): AndroidViewModel(app) {

    lateinit var todoList: ArrayList<Task>
    lateinit var doneList: ArrayList<Task>

    lateinit var listSubjectColor: ArrayList<SubjectColor>
    lateinit var mapSubjectColor: HashMap<String, Int>
    lateinit var listSubjects: ArrayList<String>

    lateinit var listCardColors: ArrayList<CardColor>
    lateinit var listColors: ArrayList<Int>

    lateinit var listSettingsItems: ArrayList<SettingsItem>

    lateinit var consolidatedListTodo: ArrayList<ListItem>
    lateinit var consolidatedListDone: ArrayList<ListItem>

    private lateinit var mapOfTodoTasks: MutableMap<Int, List<Task>>

    private var mapInitialized = false
    private var taskListsInitialized = false

    fun createTaskLists() {
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
                            if (!autoDelete(task)) { // if this task should not be auto deleted, add to doneList
                                doneList.add(task)
                            }
                        }
                    }
                }
            }
            saveJsonTaskLists() // note that in above code, auto deleted tasks were not added to doneList, but are still part of the json file.
        // this line saves the new todoList and doneList so tasks which were not added into doneList are actually removed - newly saved json file doesn't contain them
        }
        taskListsInitialized = true
    }

    private fun autoDelete(task: Task): Boolean {
        var autoDeleteDays = 0
        when (listSettingsItems[2].option) { // sets the number of days before auto deleted according to pre-saved user preference
            0 -> autoDeleteDays = 1
            1 -> autoDeleteDays = 7
            2 -> autoDeleteDays = 10
            3 -> autoDeleteDays = 30
            // else remain at 0 (this represents 'never')
        }

        return if (autoDeleteDays != 0) { // if == 0, that represents 'never'
            val expiryDate: Calendar = intToCalendar(task.completedDate)
            expiryDate.add(Calendar.DATE, 1)
            val today = Calendar.getInstance()

            val todayInt = calendarToInt(today)
            val expiryDateInt = calendarToInt(expiryDate)

            todayInt >= expiryDateInt // return true if today >= expiryDate, false otherwise
        } else {
            false
        }
    }

    fun createConsolidatedListTodo() {
        val today = Calendar.getInstance()
        val todayInt = calendarToInt(today)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DATE, 1) // adds a day to today's date
        val tomorrowInt = calendarToInt(tomorrow)

        val nextWeek = Calendar.getInstance()
        nextWeek.add(Calendar.DATE, 7) // adds 7 days to today's date
        val nextWeekInt = calendarToInt(nextWeek)

        todoList.sortBy { it.dueDateInt }

        // headings will be - Overdue, Today, Tomorrow, Next Week, Upcoming
        var overdueHeader = false
        var todayHeader = false
        var tomorrowHeader = false
        var nextWeekHeader = false
        var upcomingHeader = false // these will be set to true as headers are added into consolidatedLists

        // create sectioned list to be passed into respective rv's
        val groupedMap1: Map<Int, List<Task>> = todoList.groupBy {
            it.dueDateInt
        } // creates a map of 'date' to a 'list of Tasks' - eg: key '20160605', value is a list containing 'name 2', 'name 3'
        consolidatedListTodo = arrayListOf()
        for (dateInt: Int in groupedMap1.keys) {
            if (dateInt < todayInt) {
                if (!overdueHeader) {
                    consolidatedListTodo.add(HeaderItem(app.resources.getString(R.string.overdue))) // adds a header if one doesn't already exist
                    overdueHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedListTodo.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt == todayInt) {
                if (!todayHeader) {
                    consolidatedListTodo.add(HeaderItem(app.resources.getString(R.string.due_today))) // adds a header if one doesn't already exist
                    todayHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedListTodo.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt == tomorrowInt) {
                if (!tomorrowHeader) {
                    consolidatedListTodo.add(HeaderItem(app.resources.getString(R.string.due_tomorrow))) // adds a header if one doesn't already exist
                    tomorrowHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedListTodo.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt < nextWeekInt) {
                if (!nextWeekHeader) {
                    consolidatedListTodo.add(HeaderItem(app.resources.getString(R.string.due_soon))) // adds a header if one doesn't already exist
                    nextWeekHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedListTodo.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else {
                if (!upcomingHeader) {
                    consolidatedListTodo.add(HeaderItem(app.resources.getString(R.string.upcoming))) // adds a header if one doesn't already exist
                    upcomingHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedListTodo.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }
            }
        }
    }

    @JvmName("getConsolidatedListTodo1")
    fun getConsolidatedListTodo(): ArrayList<ListItem> {
        return consolidatedListTodo
    }

    fun createConsolidatedListDone() {
        // consolidatedListDone will not have DateItems
        doneList.sortBy { it.dueDateInt }
        consolidatedListDone = arrayListOf()
        for (t in doneList) {
            consolidatedListDone.add(TaskItem(t.subject, t.task, t.dueDate, t.notes))
        }
    }

    @JvmName("getConsolidatedListDone1")
    fun getConsolidatedListDone(): ArrayList<ListItem> {
        return consolidatedListDone
    }

    fun taskCompleted(completedTask: Task, actualIndex: Int) { // moves completedTask from todoList to doneList
        completedTask.status = true // set to 'done'
        completedTask.completedDate = calendarToInt(Calendar.getInstance()) // sets completedDate to today's date
        doneList.add(completedTask)
        todoList.removeAt(actualIndex)
        saveJsonTaskLists()
        createMapOfTodoTasks()
        createConsolidatedListDone()
    }

    fun restoreTask(restoredTask: Task, actualIndex: Int) { // moves restoredTask from doneList to todoList
        restoredTask.status = false // set to 'undone'
        restoredTask.completedDate = 0 // removes completedDate
        todoList.add(restoredTask)
        doneList.removeAt(actualIndex)
        saveJsonTaskLists()
        createMapOfTodoTasks()
        createConsolidatedListTodo()
    }

    fun clearDoneList() { // clears all items in doneList
        doneList = arrayListOf() // sets doneList to an empty list
        saveJsonTaskLists()
        createConsolidatedListDone()
    }

    fun saveJsonTaskLists() {
        todoList.sortBy { it.dueDateInt }
        doneList.sortBy { it.dueDateInt }
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
        if (!taskListsInitialized) {
            createTaskLists()
        }
        return todoList
    }

    @JvmName("getDoneList1")
    fun getDoneList(): ArrayList<Task> {
        return doneList
    }

    fun initSubjectColor() {
        listSubjectColor = arrayListOf()
        mapSubjectColor = hashMapOf()
        listSubjects = arrayListOf()

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

                        mapSubjectColor[subject] = color
                        listSubjects.add(subject)
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

    @JvmName("getListSubjects1")
    fun getListSubjects(): ArrayList<String> {
        return listSubjects
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

    fun initListSettings() {
        listSettingsItems = arrayListOf()

        val file = File(app.filesDir, "fileSettingsItems")
        if (file.exists()) {
            // deserialize and read json
            val fileJson = file.readText() // read file
            // convert fileJson into list
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val settingsItem = Klaxon().parse<SettingsItem>(reader)
                        listSettingsItems.add(settingsItem!!)
                    }
                }
            }
        }
    }

    fun getListSettings(): ArrayList<SettingsItem> {
        return listSettingsItems
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

    fun updateSettings(pos: Int, option: Int) {
        listSettingsItems[pos].option = option
        val file = Klaxon().toJsonString(listSettingsItems)
        app.openFileOutput("fileSettingsItems", Context.MODE_PRIVATE).use {
            it.write(file.toByteArray())
        }
    }

    // these methods are called when user filters results in FragmentTodo / FragmentDone
    // the above 'createConsolidatedLists' methods were not reused for this purpose so the original lists don't get changed
    fun createFilteredConsolidatedTodoList(tasks: ArrayList<Task>): ArrayList<ListItem> {
        val today = Calendar.getInstance()
        val todayInt = calendarToInt(today)

        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DATE, 1) // adds a day to today's date
        val tomorrowInt = calendarToInt(tomorrow)

        val nextWeek = Calendar.getInstance()
        nextWeek.add(Calendar.DATE, 7) // adds 7 days to today's date
        val nextWeekInt = calendarToInt(nextWeek)

        tasks.sortBy { it.dueDateInt }

        // headings will be - Overdue, Today, Tomorrow, Next Week, Upcoming
        var overdueHeader = false
        var todayHeader = false
        var tomorrowHeader = false
        var nextWeekHeader = false
        var upcomingHeader = false // these will be set to true as headers are added into consolidatedLists

        // create sectioned list to be passed into respective rv's
        val groupedMap1: Map<Int, List<Task>> = tasks.groupBy {
            it.dueDateInt
        } // creates a map of 'date' to a 'list of Tasks' - eg: key '20160605', value is a list containing 'name 2', 'name 3'
        val consolidatedList = arrayListOf<ListItem>()
        for (dateInt: Int in groupedMap1.keys) {
            if (dateInt < todayInt) {
                if (!overdueHeader) {
                    consolidatedList.add(HeaderItem(app.resources.getString(R.string.overdue))) // adds a header if one doesn't already exist
                    overdueHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedList.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt == todayInt) {
                if (!todayHeader) {
                    consolidatedList.add(HeaderItem(app.resources.getString(R.string.due_today))) // adds a header if one doesn't already exist
                    todayHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedList.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt == tomorrowInt) {
                if (!tomorrowHeader) {
                    consolidatedList.add(HeaderItem(app.resources.getString(R.string.due_tomorrow))) // adds a header if one doesn't already exist
                    tomorrowHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedList.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else if (dateInt < nextWeekInt) {
                if (!nextWeekHeader) {
                    consolidatedList.add(HeaderItem(app.resources.getString(R.string.due_soon))) // adds a header if one doesn't already exist
                    nextWeekHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedList.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }

            } else {
                if (!upcomingHeader) {
                    consolidatedList.add(HeaderItem(app.resources.getString(R.string.upcoming))) // adds a header if one doesn't already exist
                    upcomingHeader = true
                }
                val groupItems: List<Task>? = groupedMap1[dateInt] // groupItems is a list of Tasks which corresponds to the above 'date'
                groupItems?.forEach {
                    consolidatedList.add(TaskItem(it.subject, it.task, it.dueDate, it.notes)) // creates a TaskItem class for each 'name' in above list
                }
            }
        }
        return consolidatedList
    }

    fun createFilteredConsolidatedDoneList(tasks: ArrayList<Task>): ArrayList<ListItem> {
        tasks.sortBy { it.dueDateInt }
        val consolidatedList = arrayListOf<ListItem>()
        for (t in tasks) {
            consolidatedList.add(TaskItem(t.subject, t.task, t.dueDate, t.notes))
        }
        return consolidatedList
    }

    fun createMapOfTodoTasks() {
        mapOfTodoTasks = mutableMapOf()
        var list: ArrayList<Task> = arrayListOf()
        var key: Int? = null

        if (!taskListsInitialized) { // when returning to CalendarFragment from AddTaskActivity, todoList is sometimes not initialized - unsure why but this is a workaround
            createTaskLists()
        }

        for (task in todoList) {
            if (key != null) { // not the first item in list
                if (key == task.dueDateInt) { // this task is on the same date as the other tasks in list
                    list.add(task)

                } else { // this task is due on a new date
                    mapOfTodoTasks[key] = list // save list of tasks before this new tasks to map
                    key = task.dueDateInt // set new key
                    list = arrayListOf() // reset list
                    list.add(task) // add new task to new list
                }

            } else { // first item in list
                key = task.dueDateInt
                list.add(task)
            }
        }
        if (key != null && list.isNotEmpty()) {
            mapOfTodoTasks[key] = list // save last-added <Int, List> pair
        }
        mapInitialized = true
    }

    fun getMapOfTodoTasks(): Map<Int, List<Task>> {
        if (!mapInitialized) {
            createMapOfTodoTasks()
        }
        return mapOfTodoTasks
    }
}