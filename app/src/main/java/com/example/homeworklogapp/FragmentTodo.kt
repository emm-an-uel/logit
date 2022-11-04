package com.example.homeworklogapp

import android.content.Intent
import android.os.Bundle
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

class FragmentTodo : Fragment() {

    lateinit var RVTodo: RecyclerView
    lateinit var RVAdapter: MainLogRVAdapter
    lateinit var todoList: ArrayList<Task>
    private var _binding: FragmentTodoBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listColors: ArrayList<Color>

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

        // initialize map
        mapSubjectColor = hashMapOf()

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
        RVAdapter = MainLogRVAdapter(todoList, mapSubjectColor, listColors)

        // set adapter to recycler view
        RVTodo.adapter = RVAdapter

        swipeFunctions()

        // item click listener
        RVAdapter.setOnItemClickListener(object: MainLogRVAdapter.onItemClickListener {
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

            if (mapSubjectColor.size > 0) { // if mapSubjectColor already exists (ie not the first time loading up this fragment)
                createRV() // createRV() is called here to reflect changes when user swipes

            } else { // if it is the first time loading up this fragment
                setFragmentResultListener("rqMapSubjectColorTodo") { requestKey, bundle ->
                    mapSubjectColor = bundle.getSerializable("mapSubjectColor")!! as HashMap<String, Int>
                    createRV() // createRV() is called here only after getting mapSubjectColor (if it's the first time loading up this fragment)
                }
            }
        }

        // retrieve listColors
        setFragmentResultListener("rqListColorsTodo") { requestKey, bundle ->
            listColors = bundle.getParcelableArrayList("listColors")!!
        }
    }

    private fun taskCompleted(completedTask: Task) {
        setFragmentResult("rqCompletedTask", bundleOf("bundleCompletedTask" to completedTask))
    }
}