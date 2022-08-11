package com.example.homeworklogapp

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.JsonReader
import com.beust.klaxon.Klaxon
import com.example.homeworklogapp.databinding.FragmentDoneBinding
import java.io.File
import java.io.StringReader

class FragmentDone : Fragment() {

    lateinit var RVTodo: RecyclerView
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun swipeFunctions() {
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
                taskDeleted(deletedTask)

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                doneList.removeAt(viewHolder.adapterPosition)

                // below line is to notify our item is removed from adapter.
                RVAdapter.notifyItemRemoved(viewHolder.adapterPosition)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RVTodo)
    }

    private fun createRV() {
        RVTodo = binding.rvDone
        doneList = ArrayList()
        allList = ArrayList()
        RVAdapter = RVAdapter(doneList)

        // set adapter to recycler view
        RVTodo.adapter = RVAdapter

        // adding data to list
        readJson()

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
}