package com.example.homeworklogapp

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.homeworklogapp.databinding.FragmentEditTaskDialogBinding
import com.example.homeworklogapp.databinding.FragmentTodoBinding

class FragmentEditTaskDialog (private val selectedTask: Task) : DialogFragment() {

    private var _binding: FragmentEditTaskDialogBinding? = null

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditTaskDialogBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvTask.text = selectedTask.task
        binding.tvSubject.text = selectedTask.subject
        binding.tvDueDate.text = selectedTask.dueDate

        // btn Edit
        binding.btnEdit.setOnClickListener() {

            // start ActivityAddTask
            val intent = Intent(activity, ActivityAddTask::class.java)
            intent.putExtra("taskId", selectedTask.id)
            activity?.startActivity(intent)
        }
    }
}