package com.example.homeworklogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import java.io.File
import java.io.StringReader

class ActivityMainLog : AppCompatActivity() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var fabTask: FloatingActionButton
    lateinit var allList: ArrayList<Task>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_log)

        // initialize allList
        allList = ArrayList()

        // tab layout
        tabLayout = findViewById(R.id.tabLayout)
        viewPager = findViewById(R.id.viewPager)
        val tabTitles = listOf("to do", "done")
        val adapter = TabLayoutAdapter(supportFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()

        // read "fileAssignment"
        readJson()

        // fabTask
        fabTask = findViewById(R.id.fabTask)

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                if (position == 0) {
                    fabTask.setImageResource(android.R.drawable.ic_input_add)
                    fabTask.setOnClickListener() {
                        val intent = Intent(this@ActivityMainLog, ActivityAddTask::class.java)
                        startActivity(intent)
                    }

                } else {
                    fabTask.setImageResource(R.drawable.icon_trash)
                    fabTask.setOnClickListener() {
                        //confirmClearAll() todo: implement this 
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                fabTask.setOnClickListener(null) // removes onClickListener
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
    }

    private fun readJson() {
        val files = this.fileList()
        if (files.size > 1) { // if "fileAssignment" exists, since files[0] is a default-added file

            val file = File(this.filesDir, "fileAssignment")

            // deserialize and read json
            val fileJson = file.readText() // read file

            // convert fileJson into list
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val t = Klaxon().parse<Task>(reader)
                        allList.add(t!!) // add all tasks to allList
                    }
                }
            }
        }

        allList.sortBy { it.dateInt }
    }

    @JvmName("getAllList1")
    fun getAllList(): ArrayList<Task> {
        return allList
    }
}