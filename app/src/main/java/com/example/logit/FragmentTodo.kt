package com.example.logit

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.databinding.FragmentTodoBinding

class FragmentTodo : Fragment() {

    lateinit var tvEmptyList: TextView
    lateinit var rvTodo: RecyclerView
    lateinit var rvAdapter: RVAdapterMain
    lateinit var todoList: ArrayList<Task>
    lateinit var consolidatedList: ArrayList<ListItem>
    lateinit var mapOfIndex: MutableMap<Int, Int>

    private var _binding: FragmentTodoBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModelMainLog

    lateinit var listSettingsItems: ArrayList<SettingsItem>
    private var glow = false
    private var bars = false
    // TODO: define future user preferences here //

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

        // create mapOfIndex <position, actualIndex>
        createMapOfIndex()

        // display "no upcoming assignments" message if consolidatedList is empty
        tvEmptyList = view.findViewById(R.id.tvEmptyList)
        checkForEmptyList()

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

    private fun checkForEmptyList() {
        if (consolidatedList.size == 0) {
            tvEmptyList.visibility = View.VISIBLE
        } else {
            tvEmptyList.visibility = View.GONE
        }
    }

    private fun createMapOfIndex() {
        mapOfIndex = mutableMapOf()
        var index = 0
        for (n in 0 until consolidatedList.size) {
            if (consolidatedList[n].type == ListItem.TYPE_TASK) { // only create key-value pairs for TaskItems
                mapOfIndex[n] = index
                index++
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // refresh recyclerView
    override fun onResume() {
        super.onResume()
        getSettings()
        getLists()
        createMapOfIndex()
        checkForEmptyList()

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

            override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is RVAdapterMain.DateViewHolder) return 0 // prevents DateViewHolders from getting swiped
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                consolidatedList.removeAt(pos) // removes this item from consolidatedList
                val actualIndex = mapOfIndex[pos]!!
                val completedTask: Task = todoList[actualIndex]
                viewModel.taskCompleted(completedTask, actualIndex) // removes this task from todoList and adds to doneList
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                updateMapOfIndex(pos, true)
                checkForDoubleDate(pos)
                checkForEmptyList()
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(rvTodo)
    }

    private fun updateMapOfIndex(pos: Int, indexChanged: Boolean) {
        mapOfIndex.remove(pos) // remove the key-value pair of the swiped item

        // adjust the following key-value pairs
        if (indexChanged) { // TaskItem got removed
            for (p in pos+1 until consolidatedList.size+1) {
                if (mapOfIndex.containsKey(p)) { // if it doesn't contain p, that means there is a DateItem in that position (not a TaskItem)
                    val oldValue = mapOfIndex[p]!!
                    mapOfIndex.remove(p)
                    mapOfIndex[p-1] = oldValue-1
                }
            }
        } else { // DateItem got removed
            for (p in pos+1 until consolidatedList.size+1) {
                if (mapOfIndex.containsKey(p)) {
                    val actualIndex = mapOfIndex[p]!!
                    mapOfIndex.remove(p)
                    mapOfIndex[p-1] = actualIndex // actualIndex of TaskItems remains unchanged
                }
            }
        }
    }

    private fun checkForDoubleDate(removedIndex: Int) {
        if (removedIndex < consolidatedList.size) {
            if (consolidatedList[removedIndex].type == ListItem.TYPE_HEADER) {
                if (consolidatedList[removedIndex-1].type == ListItem.TYPE_HEADER) {
                    // if both a) the item which has replaced the one just removed, and b) the previous item are DateItems
                    consolidatedList.removeAt(removedIndex-1) // remove the double date (ie the one that has no TaskItems below it)
                    rvAdapter.notifyItemRemoved(removedIndex-1)
                    updateMapOfIndex(removedIndex-1, false)
                }
            }
        } else { // if item removed was the last item in list
            if (consolidatedList[removedIndex-1].type == ListItem.TYPE_HEADER) {
                consolidatedList.removeAt(removedIndex-1)
                rvAdapter.notifyItemRemoved(removedIndex-1)
                updateMapOfIndex(removedIndex-1, false)
            }
        }
    }

    private fun createRV() {
        rvTodo = binding.rvTodo
        rvAdapter = RVAdapterMain(consolidatedList, mapSubjectColor, listCardColors, glow, bars)

        // set adapter to recycler view
        rvTodo.adapter = rvAdapter

        swipeFunctions()

        // item click listener
        rvAdapter.setOnItemClickListener(object: RVAdapterMain.OnItemClickListener {
            override fun onItemClick(position: Int) {

                val actualIndex = mapOfIndex[position]!!
                val selectedTask = todoList[actualIndex]

                // start ActivityAddTask
                val intent = Intent(activity, ActivityAddTask::class.java)
                intent.putExtra("taskId", selectedTask.id)
                val listSubjects: ArrayList<String> = viewModel.getListSubjects()
                intent.putExtra("listSubjects", listSubjects)
                activity?.startActivity(intent)
            }

        })

        rvAdapter.notifyDataSetChanged()
    }

    private fun getLists() {
        todoList = viewModel.getTodoList()
        consolidatedList = viewModel.getConsolidatedListTodo()
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
                    glow = intToBoolean(listSettingsItems[i].option)
                }
                1 -> { // header bars
                    bars = intToBoolean(listSettingsItems[i].option)
                }
                // TODO: implement future user preferences here //
            }
        }
    }

    private fun intToBoolean(option: Int): Boolean {
        return option != 0
    }
}