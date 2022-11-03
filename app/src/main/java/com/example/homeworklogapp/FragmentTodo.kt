package com.example.homeworklogapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworklogapp.databinding.FragmentTodoBinding
import javax.security.auth.Subject

class FragmentTodo : Fragment() {

    lateinit var RVTodo: RecyclerView
    lateinit var RVAdapter: RVAdapter
    lateinit var todoList: ArrayList<Task>
    private var _binding: FragmentTodoBinding? = null

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // retrieve bundle
        getFromBundle()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // refresh recyclerView
    override fun onResume() {
        super.onResume()
        getFromBundle()
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
                val completedTask: Task = todoList[viewHolder.adapterPosition]

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                todoList.removeAt(viewHolder.adapterPosition)

                // below line is to notify our item is removed from adapter.
                RVAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                taskCompleted(completedTask)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RVTodo)
    }

    private fun createRV() {
        RVTodo = binding.rvTodo
        RVAdapter = RVAdapter(todoList, listSubjectColor)

        // set adapter to recycler view
        RVTodo.adapter = RVAdapter

        swipeFunctions()

        // item click listener
        RVAdapter.setOnItemClickListener(object: RVAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val selectedTask = todoList[position]

                // start ActivityAddTask
                val intent = Intent(activity, ActivityAddTask::class.java)
                intent.putExtra("taskId", selectedTask.id)
                activity?.startActivity(intent)
            }

        })

        RVAdapter.notifyDataSetChanged()
    }

    private fun getFromBundle() {
        setFragmentResultListener("rqTodoList") { requestKey, bundle ->
            todoList = bundle.getParcelableArrayList("todoList")!!

            setFragmentResultListener("rqListSubjectColor") { requestKey, bundle ->
                listSubjectColor = bundle.getParcelableArrayList("listSubjectColor")!!
                createRV()
            }
        }
    }

    private fun taskCompleted(completedTask: Task) {
        setFragmentResult("rqCompletedTask", bundleOf("bundleCompletedTask" to completedTask))
    }
}