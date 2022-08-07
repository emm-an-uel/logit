package com.example.homeworklogapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.beust.klaxon.JsonReader
import com.example.homeworklogapp.databinding.FragmentTodoBinding
import java.io.File
import java.io.StringReader

class FragmentTodo : Fragment() {

    private fun createRV() {
        RVTodo = binding.rvTodo
        taskList = ArrayList()
        RVAdapter = RVAdapter(taskList)

        // set adapter to recycler view
        RVTodo.adapter = RVAdapter

        // adding data to list
        readJson()

        RVAdapter.notifyDataSetChanged()
    }

    private fun readJson() {

        val files = requireContext().fileList()
        val numFiles = files.size

        if (numFiles > 1) { // if "json_file" exists, since files[0] is a default-added file

            // check if file exists
            val file = File(requireContext().filesDir, "json_file")

            // * deserialize and read .json *
            // read json file
            val fileJson = file.readText() // todo: app crashes here

            // convert fileJson into listPerson: List
            JsonReader(StringReader(fileJson)).use { reader ->
                reader.beginArray {
                    while (reader.hasNext()) {
                        val t = Klaxon().parse<Task>(reader)
                        taskList.add(t!!)
                    }
                }
            }
        }
    }

    lateinit var RVTodo: RecyclerView
    lateinit var RVAdapter: RVAdapter
    lateinit var taskList: ArrayList<Task>

    private var _binding: FragmentTodoBinding? = null

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // create recycler view
        createRV()

        // fabAddTask
        binding.fabAddTask.setOnClickListener() {
            val intent = Intent(activity, ActivityAddTask::class.java)
            activity?.startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}