package com.example.logit.settings

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.logit.R
import com.example.logit.log.SubjectColor
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancytoastlib.FancyToast
import java.io.File
import java.io.StringReader

class ColorCodesSettingsActivity : AppCompatActivity() {
    lateinit var rvColorCodes: RecyclerView
    lateinit var rvAdapter: RVAdapterSettingsColorCodes

    lateinit var listSubjectColor: ArrayList<SubjectColor>
    lateinit var listSubject: ArrayList<String>

    lateinit var fabAddColor: FloatingActionButton

    lateinit var listColors: ArrayList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_color_codes)

        initListColors()

        initListSubjectColor()

        fabAddColor = findViewById(R.id.fabAddColor)
        fabAddColor.setOnClickListener {
            addSubjectColor()
        }

        setupRecyclerView()
    }

    private fun initListColors() {
        listColors = arrayListOf(
            R.color.cardview_blue,
            R.color.cardview_red,
            R.color.cardview_yellow,
            R.color.cardview_green,
            R.color.cardview_purple
        )
    }

    private fun initListSubjectColor() {

        listSubjectColor = arrayListOf()
        listSubject = arrayListOf()

        val file = File(this.filesDir, "listSubjectColor")

        if (file.exists()) { // read from json file
            val fileJson = file.readText()
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val subjectColor = Klaxon().parse<SubjectColor>(reader)
                        listSubjectColor.add(subjectColor!!)
                        listSubject.add(subjectColor.subject)
                    }
                }
            }
        } else {
            val subjectColor = SubjectColor("", 0)
            listSubjectColor.add(subjectColor)
            listSubject.add("")
        }
    }

    private fun setupRecyclerView() {
        rvColorCodes = findViewById(R.id.rvSettings)
        rvAdapter = RVAdapterSettingsColorCodes(listSubjectColor, listColors)
        rvColorCodes.adapter = rvAdapter

        swipeFunctions()
    }

    private fun swipeFunctions() {
        ItemTouchHelper(object: ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val etSubject = viewHolder.itemView.findViewById<EditText>(R.id.etSubject)
                val subject = etSubject.text.toString().trim()
                if (subject != "") { // show an AlertDialog only if subject isn't blank

                    val spinnerColor = viewHolder.itemView.findViewById<Spinner>(R.id.spinnerColor)
                    val colorIndex = spinnerColor.selectedItemPosition

                    val subjectColor = SubjectColor(subject, colorIndex)
                    val position = viewHolder.adapterPosition

                    listSubjectColor[position] = subjectColor // updates listSubjectColor to reflect the current text in EditText
                    listSubject[position] = subject // updates listSubject
                    confirmDelete(subjectColor, position)
                }

                listSubjectColor.removeAt(viewHolder.adapterPosition)
                listSubject.removeAt(viewHolder.adapterPosition)
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(rvColorCodes)
    }

    private fun confirmDelete(subjectColor: SubjectColor, position: Int) {
        var touched = false

        // alert dialog
        val alertDialog: AlertDialog = this.let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Confirm") {
                        dialog, id ->
                    touched = true
                    checkDuplicates()
                }

                setNegativeButton("Cancel") {
                        dialog, id ->
                    cancelDelete(subjectColor, position)
                    touched = true
                }
            }

            val subject = subjectColor.subject
            builder.setMessage("Remove ${subject}'s color code?")

            builder.create()
        }

        alertDialog.show()
        val actualColorAccent = getColor(this, androidx.appcompat.R.attr.colorAccent)

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actualColorAccent)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actualColorAccent)

        alertDialog.setOnDismissListener {
            if (!touched) {
                cancelDelete(subjectColor, position)
            }
        }
    }

    private fun cancelDelete(subjectColor: SubjectColor, position: Int) {
        listSubjectColor.add(position, subjectColor)
        listSubject.add(position, subjectColor.subject)
        rvAdapter.notifyItemInserted(position)
    }

    private fun addSubjectColor() {

        updateList()

        // add new item in recycler view
        val newSubjectColor = SubjectColor("", 0) // adds an empty subject string with color blue
        listSubjectColor.add(newSubjectColor)

        listSubject.add("")
        rvAdapter.notifyDataSetChanged()
    }

    private fun updateList() {

        val newListSubjectColor = arrayListOf<SubjectColor>()

        val itemCount = rvColorCodes.adapter!!.itemCount
        for (i in 0 until itemCount) { // add all subjectColor to newListSubjectColor
            val holder = rvColorCodes.findViewHolderForAdapterPosition(i)
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

        listSubjectColor.clear()
        listSubjectColor.addAll(newListSubjectColor)

        // update listSubject
        val newListSubject = arrayListOf<String>()

        for (subjectColor in listSubjectColor) {
            val subject = subjectColor.subject
            newListSubject.add(subject)
        }

        listSubject.clear()
        listSubject.addAll(newListSubject)
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

                return true
            } else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveSubjectColors() {

        updateList()

        // only saves if there's no duplicate subjects
        if (noDuplicates()) { // if returns true (ie no duplicates)
            val file = Klaxon().toJsonString(listSubjectColor)
            this.openFileOutput("listSubjectColor", Context.MODE_PRIVATE).use {
                it.write(file.toByteArray())
            }

            FancyToast.makeText(this, "Saved", FancyToast.LENGTH_SHORT, FancyToast.DEFAULT, false).show()
            finish()

        } else {
            Snackbar.make(rvColorCodes, "Please remove duplicate subjects", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun noDuplicates(): Boolean {
        val listSubjectDistinct = listSubject.distinct() // returns a list of distinct elements (ie duplicates removed)

        return listSubjectDistinct.size == listSubject.size // returns true if size == size, returns false otherwise
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    fun updateListSubject(subject: String, position: Int) {
        listSubject[position] = subject
    }

    fun checkDuplicates() {
        val itemCount = rvColorCodes.adapter!!.itemCount

        for (i in 0 until itemCount) {
            val holder = rvColorCodes.findViewHolderForAdapterPosition(i)
            if (holder != null) {
                val etSubject = holder.itemView.findViewById<EditText>(R.id.etSubject)
                val subject = etSubject.text.toString().trim()

                val count = listSubject.count { it == subject }

                if (count > 1) {
                    etSubject.setTextColor(ContextCompat.getColor(this, R.color.red))
                } else {
                    etSubject.setTextColor(getColor(this, com.google.android.material.R.attr.colorOnSecondary))
                }
            }
        }
    }
}
