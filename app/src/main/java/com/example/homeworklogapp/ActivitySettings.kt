package com.example.homeworklogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import javax.security.auth.Subject

class ActivitySettings : AppCompatActivity() {

    lateinit var rvSettings: RecyclerView
    lateinit var rvAdapter: SettingsRVAdapter

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    lateinit var fabAddColor: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initListSubjectColor()

        fabAddColor = findViewById(R.id.fabAddColor)
        fabAddColor.setOnClickListener {
            addSubjectColor()
        }

        if (listSubjectColor.size == 0) { // if list is empty
            val blankSubjectColor = SubjectColor("", R.color.blue) // add a blank subjectColor
            listSubjectColor.add(blankSubjectColor)
        }
        setupRecyclerView()
    }

    private fun initListSubjectColor() {
        listSubjectColor = arrayListOf()
        // TODO: read json
    }

    private fun setupRecyclerView() {
        rvSettings = findViewById(R.id.rvSettings)
        rvAdapter = SettingsRVAdapter(listSubjectColor)
        rvSettings.adapter = rvAdapter
    }

    private fun addSubjectColor() {
        // add new item in recycler view
        val newSubjectColor = SubjectColor("", R.color.blue) // adds an empty subject string
        listSubjectColor.add(newSubjectColor)

        rvAdapter.notifyDataSetChanged()
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
        
    }
}