package com.example.homeworklogapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ActivitySettings : AppCompatActivity() {
    lateinit var rvSettings: RecyclerView
    lateinit var rvAdapter: SettingsRVAdapter

    lateinit var listSubjectColor: ArrayList<SubjectColor>
    lateinit var listSubject: ArrayList<String>

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
            val blankSubjectColor = SubjectColor("", 0) // add a blank subjectColor with color blue (since its index is 0)
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
        listSubject = arrayListOf()

        val bundle = intent.extras
        if (bundle != null) {
            listSubjectColor = bundle.getParcelableArrayList("bundleListSubjectColor")!!

            for (subjectColor in listSubjectColor) {
                val subject = subjectColor.subject
                listSubject.add(subject)
            }
        }
    }

    private fun setupRecyclerView() {
        rvSettings = findViewById(R.id.rvSettings)
        rvAdapter = SettingsRVAdapter(listSubjectColor, listSubject)
        rvSettings.adapter = rvAdapter

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

                val subjectColor = listSubjectColor[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                confirmDelete(subjectColor, position)

                listSubjectColor.removeAt(viewHolder.adapterPosition)
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
        }).attachToRecyclerView(rvSettings)
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
                }

                setNegativeButton("Cancel") {
                    dialog, id ->
                    cancelDelete(subjectColor, position)
                    touched = true
                }
            }

            val subject = subjectColor.subject
            builder.setMessage("Remove ${subject}'s color code?")
            // TODO: {subject} is currently referring to the subject in listSubjectColor; doesn't reflect changes made if the user hasn't saved

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
        rvAdapter.notifyItemInserted(position)
    }

    private fun addSubjectColor() {

        updateList()

        // add new item in recycler view
        val newSubjectColor = SubjectColor("", 0) // adds an empty subject string with color blue
        listSubjectColor.add(newSubjectColor)
        rvAdapter.notifyDataSetChanged()
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

            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, ActivityMainLog::class.java)
            startActivity(intent)
            finish()

        } else {
            Toast.makeText(this, "Please remove duplicate subjects", Toast.LENGTH_SHORT).show()
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
}