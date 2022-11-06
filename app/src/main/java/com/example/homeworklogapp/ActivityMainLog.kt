package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

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

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_log)

        // instantiate viewModel
        viewModel = ViewModelProvider(this).get(ViewModel::class.java) 

        // initialize lists
        todoList = ArrayList()
        doneList = ArrayList()

        // initialize listColors
        initListColors()

        // initialize todoList and doneList in ViewModel
        initTaskLists()

        // initialize listSubjectColor and mapSubjectColor in ViewModel
        initSubjectColor()

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

                    checkFabClickability() // set clickability

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

        remoteCheckFabClickability()
    }

    private fun remoteCheckFabClickability() {
        supportFragmentManager.setFragmentResultListener("rqCheckFabClickability", this) { requestKey, bundle ->
            checkFabClickability()
        }
    }

    private fun initTaskLists() {
        viewModel.initTaskLists() // creates todoList and doneList
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
        viewModel.clearDoneList()
        doneList = viewModel.getDoneList()

        // update fragmentDone
        val bundle = Bundle()
        bundle.putInt("clearAll", 0)
        supportFragmentManager.setFragmentResult("rqClearAll", bundle)

        //passBundlesTaskLists()
        fabDisabled()
    }

    private fun checkFabClickability() {
        // faded fab and unclickable when doneList is empty
        doneList = viewModel.getDoneList()

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

    private fun initSubjectColor() {
        listSubjectColor = arrayListOf()
        mapSubjectColor = hashMapOf()

        viewModel.initSubjectColor()
        listSubjectColor = viewModel.getListSubjectColor()
    }

    private fun initListColors() {

        viewModel.initListCardColors()
        listCardColors = viewModel.getListCardColors()
    }
}
