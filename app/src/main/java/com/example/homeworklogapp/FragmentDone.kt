package com.example.homeworklogapp

import android.R
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.homeworklogapp.databinding.FragmentDoneBinding
import com.google.android.material.snackbar.Snackbar
import java.io.File
import java.io.StringReader


class FragmentDone : Fragment() {

    lateinit var RVDone: RecyclerView
    lateinit var RVAdapter: RVAdapter
    lateinit var doneList: ArrayList<Task>
    lateinit var allList: ArrayList<Task>

    private var _binding: FragmentDoneBinding? = null

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDoneBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create recycler view
        createRV()

        // swipe functions
        swipeFunctions()

        // clearAll
        binding.fabClearAll.setOnClickListener() {
            confirmClearAll()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // refresh recyclerView
    override fun onResume() {
        super.onResume()
        createRV()
    }

    private fun swipeFunctions() {

        // restore task
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val restoredTask = doneList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                restoreTask(restoredTask)

                doneList.removeAt(position)
                RVAdapter.notifyItemRemoved(position)
            }
        }).attachToRecyclerView(RVDone)

        // delete permanently
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                // this method is called
                // when the item is moved.
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // change task status
                val deletedTask: Task = doneList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition
                confirmDelete(deletedTask, position)

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                doneList.removeAt(viewHolder.adapterPosition)

                // below line is to notify our item is removed from adapter.
                RVAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RVDone)
    }

    private fun createRV() {
        RVDone = binding.rvDone
        doneList = ArrayList()
        allList = ArrayList()
        RVAdapter = RVAdapter(doneList)

        // set adapter to recycler view
        RVDone.adapter = RVAdapter

        // adding data to list
        readJson()

        // item click listener
        RVAdapter.setOnItemClickListener(object: RVAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // do nothing
            }

        })

        RVAdapter.notifyDataSetChanged()
    }

    private fun readJson() {

        val files = requireContext().fileList()
        val numFiles = files.size

        if (numFiles > 1) { // if "fileAssignment" exists, since files[0] is a default-added file

            // check if file exists
            val file = File(requireContext().filesDir, "fileAssignment")

            // * deserialize and read .json *
            // read json file
            val fileJson = file.readText()

            // convert fileJson into listPerson: List
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val t = Klaxon().parse<Task>(reader)

                        allList.add(t!!) // add task to allList either way

                        if (t.status) { // if task is done
                            doneList.add(t)
                        }
                    }
                }
            }
        }

        doneList.sortBy { it.dateInt }
    }

    private fun restoreTask(restoredTask: Task) {
        // remove task with status true from allList
        for (task in allList) {
            if (task.id == restoredTask.id) {
                allList.remove(task)
                break
            }
        }

        // change status to false
        restoredTask.status = false

        // update allList
        allList.add(restoredTask)

        // save Json
        val updatedFile = Klaxon().toJsonString(allList)
        requireContext().openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
            it.write(updatedFile.toByteArray())
        }
    }

    private fun confirmDelete(deletedTask: Task, position: Int) {
        var touched = false

        // alert dialog
        val alertDialog: AlertDialog = requireContext().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Confirm"
                ) { dialog, id ->
                    taskDeleted(deletedTask)
                    touched = true
                }

                setNegativeButton("Cancel"
                ) { dialog, id ->
                    doneList.add(position, deletedTask)
                    RVAdapter.notifyItemInserted(position)
                    touched = true
                }
            }

            builder.setMessage("Clear this task?")

            builder.create()
        }

        alertDialog.show()
        val actualColorAccent = getColor(requireContext(), R.attr.colorAccent)

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actualColorAccent)
        alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actualColorAccent)

        alertDialog.setOnDismissListener {
            if (!touched) { // if touched == false (ie user touched outside dialog box)
                doneList.add(position, deletedTask)
                RVAdapter.notifyItemInserted(position)
            }
        }
    }

    private fun taskDeleted(deletedTask: Task) {
        for (task in allList) {
            if (task.id == deletedTask.id) {
                allList.remove(task)
                break
            }
        }

        // save locally
        val updatedFile = Klaxon().toJsonString(allList)
        requireContext().openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
            it.write(updatedFile.toByteArray())
        }
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun confirmClearAll() {

        if (doneList.size > 0) { // only if there's tasks being shown
            // alert dialog
            val alertDialog: AlertDialog = requireContext().let {
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
            val actualColorAccent = getColor(requireContext(), R.attr.colorAccent)

            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actualColorAccent)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actualColorAccent)
        } else {
            Snackbar.make(requireContext(), requireView(), "No tasks to delete", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun clearAll() {
        var n = 0
        while (n < allList.size) {
            val task = allList[n]

            if (task.status) {
                allList.remove(task)
            } else {
                n++
            }
        }

        // save locally
        val updatedFile = Klaxon().toJsonString(allList)
        requireContext().openFileOutput("fileAssignment", Context.MODE_PRIVATE).use {
            it.write(updatedFile.toByteArray())
        }

        // refresh RV
        createRV()
    }
}