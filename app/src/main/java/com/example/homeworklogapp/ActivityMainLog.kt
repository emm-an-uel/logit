package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.viewpager2.widget.ViewPager2
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.StringReader

class ActivityMainLog : AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var fabTask: FloatingActionButton
    lateinit var todoList: ArrayList<Task>
    lateinit var doneList: ArrayList<Task>
    lateinit var bundleTodo: Bundle
    lateinit var bundleDone: Bundle

    lateinit var listSubjectColor: ArrayList<SubjectColor>
    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listColors: ArrayList<Color>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_log)

        // initialize lists
        todoList = ArrayList()
        doneList = ArrayList()

        // initialize listColors
        initListColors()

        // read "fileAssignment"
        readJsonFileAssignment()

        // read "listSubjectColor"
        readJsonListSubjectColor()

        // tab layout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val tabTitles = listOf("to do", "done")
        val adapter = TabLayoutAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // fabTask
        fabTask = findViewById(R.id.fabTask)

        fabTask.setOnClickListener { // default onClickListener is addTask
            val intent = Intent(this@ActivityMainLog, ActivityAddTask::class.java)
            startActivity(intent)
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position

                if (position == 0) { // if in fragmentTodo
                    fabEnabled()

                    fabTask.setImageResource(android.R.drawable.ic_input_add)
                    fabTask.setOnClickListener {
                        val intent = Intent(this@ActivityMainLog, ActivityAddTask::class.java)
                        startActivity(intent)
                    }

                } else { // if in fragmentDone
                    fabTask.setImageResource(R.drawable.icon_trash)

                    fabClickability() // set clickability

                    fabTask.setOnClickListener {
                        confirmClearAll()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                fabTask.setOnClickListener(null) // removes onClickListener
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // the following functions handle the changing of task statuses upon swiping
        completeTask()
        restoreTask()
        deleteTask()
    }

    private fun readJsonFileAssignment() { // TODO: use ViewModel to read json file then pass to ActivityMainLog for better efficiency

        val file = File(this.filesDir, "fileAssignment")

        if (file.exists()) {

            // deserialize and read json
            val fileJson = file.readText() // read file

            // convert fileJson into list
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val t = Klaxon().parse<Task>(reader)

                        if (!t!!.status) { // undone
                            todoList.add(t)
                        } else { // done
                            doneList.add(t)
                        }
                    }
                }
            }
        }

        passBundlesTaskLists()
    }

    private fun passBundlesTaskLists() {
        // sort both lists by due date
        todoList.sortBy { it.dateInt }
        doneList.sortBy { it.dateInt }

        bundleTodo = Bundle()
        bundleTodo.putParcelableArrayList("todoList", todoList)
        supportFragmentManager.setFragmentResult("rqTodoList", bundleTodo) // passes bundleTodo to FragmentManager

        bundleDone = Bundle()
        bundleDone.putParcelableArrayList("doneList", doneList)
        supportFragmentManager.setFragmentResult("rqDoneList", bundleDone)
    }

    private fun completeTask() {
        supportFragmentManager.setFragmentResultListener("rqCompletedTask", this) { requestKey, bundle ->
            val completedTask = bundle.getParcelable<Task>("bundleCompletedTask")!!

            for (task in todoList) { // remove completedTask from toDoList
                if (task.id == completedTask.id) {
                    todoList.remove(task)
                    break
                }
            }

            completedTask.status = true // set to done
            doneList.add(completedTask) // add completedTask to doneList

            passBundlesTaskLists() // update lists\
            saveJson()
        }
    }

    private fun restoreTask() {
        supportFragmentManager.setFragmentResultListener("rqRestoredTask", this) { requestKey, bundle ->
            val restoredTask = bundle.getParcelable<Task>("bundleRestoredTask")!!

            for (task in doneList) { // remove restoredTask from doneList
                if (task.id == restoredTask.id) {
                    doneList.remove(task)
                    break
                }
            }

            restoredTask.status = false // set to undone
            todoList.add(restoredTask) // add restoredTask to toDoList

            passBundlesTaskLists() // update lists
            saveJson()
            fabClickability() // set fab clickability depending on if there's any remaining tasks in doneList 
        }
    }

    private fun deleteTask() {
        supportFragmentManager.setFragmentResultListener("rqDeletedTask", this) { requestKey, bundle ->
            val deletedTask = bundle.getParcelable<Task>("bundleDeletedTask")!!

            for (task in doneList) { // remove deletedTask from doneList
                if (task.id == deletedTask.id) {
                    doneList.remove(task)
                    break
                }
            }
        }

        saveJson()
    }

    private fun saveJson() {
        // combine todoList and doneList
        val allList: ArrayList<Task> = ArrayList()
        allList.addAll(todoList)
        allList.addAll(doneList)

        // save locally as json file
        val updatedFile = Klaxon().toJsonString(allList)
        this.openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
            it.write(updatedFile.toByteArray())
        }
    }

    private fun confirmClearAll() {
        if (doneList.size > 0) { // only if there's tasks being shown
            // alert dialog
            val alertDialog: AlertDialog = this.let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton(
                        "Confirm"
                    ) { dialog, id ->
                        clearAll()
                    }

                    setNegativeButton(
                        "Cancel"
                    ) { dialog, id ->
                        // do nothing
                    }
                }

                builder.setMessage("Clear all tasks?")

                builder.create()
            }

            alertDialog.show()
            val actualColorAccent = getColor(this, android.R.attr.colorAccent)

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actualColorAccent)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actualColorAccent)
        } else {
            Snackbar.make(this, findViewById(R.id.tabLayout), "No tasks to clear", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun clearAll() { // delete all items in doneList

        doneList = ArrayList() // blank ArrayList

        saveJson()
        passBundlesTaskLists()
        fabDisabled()
    }

    private fun fabClickability() {
        // faded fab and unclickable when doneList is empty
        if (doneList.size > 0) {
            fabEnabled()
        } else {
            fabDisabled()
        }
    }

    private fun fabDisabled() {
        fabTask.isEnabled = false
        fabTask.background.alpha = 45
    }

    private fun fabEnabled() {
        fabTask.isEnabled = true
        fabTask.background.alpha = 255
    }

    // action bar stuff
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_log_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.settings -> {
                val intent = Intent(this, ActivitySettings::class.java)
                intent.putExtras(bundleOf("bundleListSubjectColor" to listSubjectColor))
                startActivity(intent)

                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun readJsonListSubjectColor() {
        listSubjectColor = arrayListOf()
        mapSubjectColor = hashMapOf()

        val file = File(this.filesDir, "listSubjectColor")

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

        passBundleSubjectColors()
    }

    private fun passBundleSubjectColors() {
        val bundleMapSubjectColor = Bundle()

        bundleMapSubjectColor.putSerializable("mapSubjectColor", mapSubjectColor)
        supportFragmentManager.setFragmentResult("rqMapSubjectColor", bundleMapSubjectColor) // passes bundleMapSubjectColor to FragmentManager
    }

    private fun initListColors() {
        listColors = arrayListOf(
            Color(R.color.blue),
            Color(R.color.red),
            Color(R.color.yellow),
            Color(R.color.green),
            Color(R.color.pink)
        )

        passBundleListColors()
    }

    private fun passBundleListColors() {
        val bundleListColors = Bundle()

        bundleListColors.putParcelableArrayList("listColors", listColors)
    }
}
