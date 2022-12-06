package com.example.homeworklogapp

import android.R
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworklogapp.databinding.FragmentDoneBinding


class FragmentDone : Fragment() {

    lateinit var rvDone: RecyclerView
    lateinit var rvAdapter: RVAdapterMain
    lateinit var doneList: ArrayList<Task>
    lateinit var consolidatedList: ArrayList<ListItem>
    lateinit var mapOfIndex: MutableMap<Int, Int>

    private var _binding: FragmentDoneBinding? = null

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

        viewModel = ViewModelProvider(requireActivity()).get(ViewModelMainLog::class.java)

        _binding = FragmentDoneBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // get settings - user preferences
        getSettings()

        // initialize map
        mapSubjectColor = hashMapOf()

        // get doneList
        getLists()

        // create mapOfIndex <position, actualIndex>
        createMapOfIndex()

        // watch for clearAll
        checkClearAll()

        // fabAddTask visibility
        rvDone.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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
        getLists()
        createMapOfIndex()

        (context as ActivityMainLog).showFabAddTask() // show fab by default
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

            override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is RVAdapterMain.DateViewHolder) return 0 // prevents DateViewHolders from getting swiped
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                consolidatedList.removeAt(pos) // removes this item from consolidatedList
                val actualIndex = mapOfIndex[pos]!!
                val restoredTask: Task = doneList[actualIndex]
                restoreTask(restoredTask, actualIndex)
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                updateMap(pos, true)
                checkForDoubleDate(pos)
            }
        }).attachToRecyclerView(rvDone)

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

            override fun getSwipeDirs (recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                if (viewHolder is RVAdapterMain.DateViewHolder) return 0 // prevents DateViewHolders from getting swiped
                return super.getSwipeDirs(recyclerView, viewHolder)
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val actualIndex = mapOfIndex[pos]!!
                val deletedTaskItem = consolidatedList[pos] as TaskItem
                consolidatedList.removeAt(pos)

                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                confirmDelete(deletedTaskItem, pos, actualIndex)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(rvDone)
    }

    private fun updateMap(pos: Int, indexChanged: Boolean) {
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
            if (consolidatedList[removedIndex].type == ListItem.TYPE_DATE) {
                if (consolidatedList[removedIndex-1].type == ListItem.TYPE_DATE) {
                    // if both a) the item which has replaced the one just removed, and b) the previous item are DateItems
                    consolidatedList.removeAt(removedIndex-1) // remove the double date (ie the one that has no TaskItems below it)
                    rvAdapter.notifyItemRemoved(removedIndex-1)
                    updateMap(removedIndex-1, false)
                }
            }
        } else { // if item removed was the last item in list
            if (consolidatedList[removedIndex-1].type == ListItem.TYPE_DATE) {
                consolidatedList.removeAt(removedIndex-1)
                rvAdapter.notifyItemRemoved(removedIndex-1)
                updateMap(removedIndex-1, false)
            }
        }
    }

    private fun createRV() {
        rvDone = binding.rvDone
        rvAdapter = RVAdapterMain(consolidatedList, mapSubjectColor, listCardColors, glow)

        // set adapter to recycler view
        rvDone.adapter = rvAdapter

        swipeFunctions()

        // item click listener
        rvAdapter.setOnItemClickListener(object: RVAdapterMain.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // do nothing
            }

        })

        rvAdapter.notifyDataSetChanged()
    }

    private fun confirmDelete(deletedTaskItem: TaskItem, position: Int, actualIndex: Int) {
        var touched = false

        // alert dialog
        val alertDialog: AlertDialog = requireContext().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Confirm"
                ) { dialog, id ->
                    deleteTask(actualIndex)
                    touched = true
                }

                setNegativeButton("Cancel"
                ) { dialog, id ->
                    consolidatedList.add(position, deletedTaskItem)
                    rvAdapter.notifyItemInserted(position)
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
                consolidatedList.add(position, deletedTaskItem)
                rvAdapter.notifyItemInserted(position)
            }
        }
    }

    private fun deleteTask(actualIndex: Int) {
        doneList.removeAt(actualIndex)
        val bundle = Bundle()
        bundle.putInt("fabClickability", 0)
        setFragmentResult("rqCheckFabClickability", bundle)
    }

    private fun restoreTask(restoredTask: Task, actualIndex: Int) {
        viewModel.restoreTask(restoredTask, actualIndex)

        // below code is just so ActivityMainLog calls checkFabClickability() when a task is restored
        val bundle = Bundle()
        bundle.putInt("fabClickability", 0)
        setFragmentResult("rqCheckFabClickability", bundle)
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun getLists() {
        doneList = viewModel.getDoneList()
        consolidatedList = viewModel.getConsolidatedListDone()
        listCardColors = viewModel.getListCardColors()

        if (mapSubjectColor.size > 0) {
            createRV()
        } else {
            mapSubjectColor = viewModel.getMapSubjectColor()
            createRV()
        }
    }

    private fun checkClearAll() {
        setFragmentResultListener("rqClearAll") { requestKey, bundle ->
            getLists()
            createMapOfIndex()
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