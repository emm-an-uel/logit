package com.example.homeworklogapp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworklogapp.databinding.FragmentTodoBinding
import kotlinx.android.synthetic.main.fragment_todo.*

class FragmentTodo : Fragment() {

    lateinit var RVTodo: RecyclerView
    lateinit var RVAdapter: RVAdapterMain
    lateinit var todoList: ArrayList<Task>
    lateinit var todoItemList: List<ListItem>

    private var _binding: FragmentTodoBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModelMainLog

    lateinit var listSettingsItems: ArrayList<SettingsItem>
    private var glow = false
    // instantiate future user preferences here //

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // instantiate viewModel
        viewModel = ViewModelProvider(requireActivity()).get(ViewModelMainLog::class.java)

        _binding = FragmentTodoBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get settings - user preferences
        getSettings()

        // initialize map
        mapSubjectColor = hashMapOf()

        // get todoList
        getLists()

        // fabAddTask visibility
        rvTodo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // scrolling down and fab is shown
                    (context as ActivityMainLog).hideFabAddTask()
                } else { // scrolling up and fab is not shown
                    (context as ActivityMainLog).showFabAddTask()
                }
            }
        })

        (context as ActivityMainLog).showFabAddTask() // show by default
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // refresh recyclerView
    override fun onResume() {
        super.onResume()
        getLists()

        (context as ActivityMainLog).showFabAddTask() // show fab by default
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
        RVAdapter = RVAdapterMain(todoItemList, mapSubjectColor, listCardColors, glow) // pass future user preferences here //

        // set adapter to recycler view
        RVTodo.adapter = RVAdapter

        swipeFunctions()

        // item click listener
        RVAdapter.setOnItemClickListener(object: RVAdapterMain.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val selectedTask = todoList[position]

                // start ActivityAddTask
                val intent = Intent(activity, ActivityAddTask::class.java)
                intent.putExtra("taskId", selectedTask.id)
                val listSubjects: ArrayList<String> = viewModel.getListSubjects()
                intent.putExtra("listSubjects", listSubjects)
                activity?.startActivity(intent)
            }

        })

        RVAdapter.notifyDataSetChanged()
    }

    private fun taskCompleted(completedTask: Task) {
        viewModel.taskCompleted(completedTask)
    }

    private fun getLists() {
        todoList = viewModel.getTodoList()
        todoItemList = viewModel.getTodoItemList()
        listCardColors = viewModel.getListCardColors()

        if (mapSubjectColor.size > 0) { // if mapSubjectColor already exists (ie not the first time loading up this fragment)
            createRV() // createRV() is called here to reflect changes when user swipes

        } else { // if mapSubjectColor is still empty (ie first time loading up this fragment)
            mapSubjectColor = viewModel.getMapSubjectColor()
            createRV()
        }
    }

    private fun getSettings() {
        listSettingsItems = viewModel.getListSettings()
        for (i in 0 until listSettingsItems.size) {
            when (i) {
                0 -> { // glow
                    glow = listSettingsItems[i].status
                }
                // implement future user preferences here //
            }
        }
    }
}