package com.example.logit.mainlog

import android.content.Intent
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.ViewModelParent
import com.example.logit.addtask.ActivityAddTask
import com.example.logit.databinding.FragmentTodoBinding
import com.example.logit.settings.SettingsItem

class FragmentTodo : Fragment() {

    lateinit var tvEmptyList: TextView
    lateinit var rvTodo: RecyclerView
    lateinit var rvAdapter: RVAdapterLog
    lateinit var todoList: ArrayList<Task>
    lateinit var consolidatedList: ArrayList<ListItem>
    lateinit var mapOfIndex: MutableMap<Int, Int>

    private var _binding: FragmentTodoBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModelParent

    lateinit var listSettingsItems: ArrayList<SettingsItem>
    private var glow = false
    private var bars = false
    // TODO: future user preferences here //

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // instantiate viewModel
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]

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
        createMapOfIndex(consolidatedList)

        // display "no upcoming assignments" message if consolidatedList is empty
        tvEmptyList = view.findViewById(R.id.tvEmptyList)
        checkForEmptyList()

        // fabAddTask visibility
        rvTodo.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                if (dy > 0) { // scrolling down and fab is shown
                    setFragmentResult("hideFab", bundleOf())
                } else { // scrolling up and fab is not shown
                    setFragmentResult("showFab", bundleOf())
                }
            }
        })
        setFragmentResult("showFab", bundleOf()) // show by default
    }

    private fun checkForEmptyList() {
        if (consolidatedList.size == 0) {
            tvEmptyList.visibility = View.VISIBLE
        } else {
            tvEmptyList.visibility = View.GONE
        }
    }

    private fun createMapOfIndex(consolidatedList: ArrayList<ListItem>) {
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
        createMapOfIndex(consolidatedList)
        checkForEmptyList()

        setFragmentResult("showFab", bundleOf())
    }

    private fun swipeFunctions() {
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is RVAdapterLog.HeaderViewHolder) return 0 // prevents HeaderViewHolders from getting swiped
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                setFragmentResult("todoListChanged", bundleOf()) // update FragmentLog
                val pos = viewHolder.adapterPosition
                consolidatedList.removeAt(pos) // removes this item from consolidatedList
                val actualIndex = mapOfIndex[pos]!!
                val completedTask: Task = todoList[actualIndex]
                viewModel.taskCompleted(completedTask, actualIndex) // removes this task from todoList and adds to doneList
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                updateMapOfIndex(pos, true)
                checkForDoubleDate(pos)
                checkForEmptyList()

                // haptic feedback
                requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
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
        rvAdapter = RVAdapterLog(consolidatedList, mapSubjectColor, listCardColors, glow, bars)

        // set adapter to recycler view
        rvTodo.adapter = rvAdapter

        swipeFunctions()

        // item click listener
        rvAdapter.setOnItemClickListener(object: RVAdapterLog.OnItemClickListener {
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

        // filter list (search function) 
        setFragmentResultListener("filterList") { _, bundle ->
            val filteredList: ArrayList<Task> = bundle.getParcelableArrayList<Task>("filteredList") as ArrayList<Task>
            viewModel.createConsolidatedListTodo(filteredList) // calls on method in ViewModel to create consolidated list
            val newConsolidatedList = viewModel.getConsolidatedListTodo()
            createMapOfIndex(newConsolidatedList) // updates mapOfIndex - in case user decides to swipe after filtering results
            rvAdapter.filterList(newConsolidatedList) // passes newly made consolidated list to adapter
        }
    }

    private fun getLists() {
        todoList = viewModel.getTodoList()
        consolidatedList = viewModel.getConsolidatedListTodo()
        listCardColors = viewModel.getListCardColors()
        mapSubjectColor = viewModel.getMapSubjectColor()
        createRV()
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
                // TODO: future user preferences here //
            }
        }
    }

    private fun intToBoolean(option: Int): Boolean {
        return option != 0
    }
}