package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.io.File
import java.io.StringReader

class ActivitySettings : AppCompatActivity() {

    lateinit var rvSettings: RecyclerView
    lateinit var rvAdapter: SettingsRVAdapter

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    lateinit var fabAddColor: FloatingActionButton

    lateinit var listColors: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initListColors()

        initListSubjectColor()

        fabAddColor = findViewById(R.id.fabAddColor)
        fabAddColor.setOnClickListener {
            addSubjectColor()
        }

        if (listSubjectColor.size == 0) { // if list is empty
            val blankSubjectColor = SubjectColor("blankSubject", 0) // add a blank subjectColor with color blue (since its index is 0)
            listSubjectColor.add(blankSubjectColor)
        }

        setupRecyclerView()
    }

    private fun initListColors() {
        listColors = arrayListOf(
            R.color.blue,
            R.color.red,
            R.color.yellow,
            R.color.green,
            R.color.pink
        )
    }

    private fun initListSubjectColor() {
        listSubjectColor = arrayListOf()

        val bundle = intent.extras
        if (bundle != null) {
            listSubjectColor = bundle.getParcelableArrayList("bundleListSubjectColor")!!
        }
    }

    private fun setupRecyclerView() {
        rvSettings = findViewById(R.id.rvSettings)
        rvAdapter = SettingsRVAdapter(listSubjectColor)
        rvSettings.adapter = rvAdapter
    }

    private fun addSubjectColor() {

        updateList()

        // add new item in recycler view
        val newSubjectColor = SubjectColor("", 0) // adds an empty subject string with color blue
        listSubjectColor.add(newSubjectColor)

        setupRecyclerView()
    }

    private fun updateList() {

        val newListSubjectColor = arrayListOf<SubjectColor>()

        val itemCount = rvSettings.adapter!!.itemCount
        for (i in 0 until itemCount) { // add all subjectColor to newListSubjectColor
            val holder = rvSettings.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                val etSubject = holder.itemView.findViewById<EditText>(R.id.etSubject)
                val subject = etSubject.text.toString().trim()

                if (subject != "") { // only add if subject isnt blank

                    val spinnerColor = holder.itemView.findViewById<Spinner>(R.id.spinnerColor)
                    val colorIndex = spinnerColor.selectedItemPosition

                    val subjectColor = SubjectColor(subject, colorIndex)
                    newListSubjectColor.add(subjectColor)
                }
            }
        }

        listSubjectColor = newListSubjectColor
    }

    // action bar stuff
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.save -> {

                saveSubjectColors()
                Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()

                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSubjectColors() {

        // TODO: prevent duplicate subjects

        updateList()

        val file = Klaxon().toJsonString(listSubjectColor)
        this.openFileOutput("listSubjectColor", Context.MODE_PRIVATE).use {
            it.write(file.toByteArray())
        }

        val intent = Intent(this, ActivityMainLog::class.java)
        startActivity(intent)
        finish()
    }
}