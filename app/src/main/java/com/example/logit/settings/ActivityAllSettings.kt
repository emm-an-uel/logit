package com.example.logit.settings

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.logit.R
import java.io.File
import java.io.StringReader

class ActivityAllSettings : AppCompatActivity() {

    lateinit var rvSettings: RecyclerView
    lateinit var rvAdapter: RVAdapterAllSettings

    lateinit var listSettingsItems: ArrayList<SettingsItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_settings)

        getSettings()

        setupRVSettings()
    }

    private fun getSettings() {
        listSettingsItems = arrayListOf()

        val file = File(this.filesDir, "fileSettingsItems")

        if (file.exists()) { // read from json file
            val fileJson = file.readText()
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val settingsItem = Klaxon().parse<SettingsItem>(reader)
                        listSettingsItems.add(settingsItem!!)
                    }
                }
            }
        } else { // if file doesn't exist, populate with default settings
            listSettingsItems.apply {
                add(SettingsItem("Background glow", 0))
                add(SettingsItem("Header bars", 0))
                add(SettingsItem("Automatically delete completed tasks", 3)) // '30 days' by default
                add(SettingsItem("Edit subject color codes", 0))
                // TODO: add additional settings items here //
            }
        }
    }

    private fun setupRVSettings() {
        rvSettings = findViewById(R.id.rvSettings)
        rvAdapter = RVAdapterAllSettings(listSettingsItems)
        rvSettings.adapter = rvAdapter

        rvSettings.addItemDecoration(DividerItemDecoration(this, LinearLayoutManager.VERTICAL))
    }

    fun updateSettings(position: Int, option: Int) {
        listSettingsItems[position].option = option
        saveSettings()
    }

    private fun saveSettings() {
        val file = Klaxon().toJsonString(listSettingsItems)

        this.openFileOutput("fileSettingsItems", Context.MODE_PRIVATE).use {
            it.write(file.toByteArray())
        }
    }
}
