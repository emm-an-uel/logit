package com.example.homeworklogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.RecyclerView
import javax.security.auth.Subject

class ActivitySettings : AppCompatActivity() {

    lateinit var rvSettings: RecyclerView
    lateinit var rvAdapter: SettingsRVAdapter

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        initListSubjectColor()

        if (listSubjectColor.size != 0) { // if list is not empty
            setupRecyclerView()
        }
    }

    private fun initListSubjectColor() {
        listSubjectColor = arrayListOf()
        // TODO: read json

        // temporary method below
        val subjectColor1 = SubjectColor("Subject 1", R.color.blue)
        val subjectColor2 = SubjectColor("Subject 2", R.color.red)

        listSubjectColor.apply {
            add(subjectColor1)
            add(subjectColor2)
        }
    }

    private fun setupRecyclerView() {
        rvSettings = findViewById(R.id.rvSettings)
        rvAdapter = SettingsRVAdapter(listSubjectColor)
        rvSettings.adapter = rvAdapter
    }
}