package com.example.logit.log

import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.ViewModelParent
import com.example.logit.databinding.FragmentDoneBinding
import com.example.logit.settings.SettingsItem
import com.google.android.material.snackbar.Snackbar
import com.shashank.sony.fancytoastlib.FancyToast


class DoneFragment : Fragment() {

    private var firstInstance = true

    lateinit var tvEmptyList: TextView
    lateinit var rvDone: RecyclerView
    lateinit var rvAdapter: RVAdapterLog
    lateinit var doneList: ArrayList<Task>
    lateinit var consolidatedList: ArrayList<ListItem>

    private var _binding: FragmentDoneBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var colors: ArrayList<Int>

    lateinit var viewModel: ViewModelParent

    lateinit var listSettingsItems: ArrayList<SettingsItem>
    private var glow = false
    private var bars = false
    // TODO: future user preferences here //

    // setup view binding
    private val binding get() = _binding!!

    // "undo" delete functionality
    private var deletedTask: Task? = null // deleted task will be stored here until another task is deleted

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        // display "no completed assignments" message if consolidatedList is empty
        tvEmptyList = binding.tvEmptyList
        checkForEmptyList()

        // watch for clearAll
        checkClearAll()

        // fabAddTask visibility
        rvDone.addOnScrollListener(object : RecyclerView.OnScrollListener() {
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // refresh recyclerView
    override fun onResume() {
        super.onResume()
        getSettings()
        getLists()
        checkForEmptyList()

        setFragmentResult("showFab", bundleOf())
    }

    private fun swipeFunctions() {

        // mark as undone
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                consolidatedList.removeAt(pos) // removes this item from consolidatedList
                val undoneTask: Task = doneList[pos]
                markAsUndone(undoneTask)
                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)
                checkForEmptyList()

                setFragmentResult("listsChanged", bundleOf()) // update FragmentLog

                // haptic feedback
                requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }).attachToRecyclerView(rvDone)

        // delete permanently
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val pos = viewHolder.adapterPosition
                val deletedTaskItem = consolidatedList[pos] as TaskItem
                consolidatedList.removeAt(pos)

                rvAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                deleteTask(pos)
                createSnackbar(deletedTaskItem, pos)

                // haptic feedback
                requireView().performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            }
        }).attachToRecyclerView(rvDone)
    }

    private fun createSnackbar(deletedTaskItem: TaskItem, pos: Int) {
        val snack = Snackbar.make(rvDone, "Task deleted", Snackbar.LENGTH_LONG)

        val customSnackView = layoutInflater.inflate(R.layout.snackbar_custom, null, false) // inflate custom snackbar layout

        if (snack.view.background != null) { // set default background to transparent
            snack.view.setBackgroundColor(ContextCompat.getColor(requireContext(), com.google.android.material.R.color.mtrl_btn_transparent_bg_color))
        }

        // add custom view
        val snackbarLayout: Snackbar.SnackbarLayout = snack.view as Snackbar.SnackbarLayout
        snackbarLayout.setPadding(5, 0, 5, 15)
        snackbarLayout.addView(customSnackView)

        // set snackbar message
        val tvSnackbar: TextView = snackbarLayout.findViewById(R.id.tvSnackbar)
        tvSnackbar.text = "Task deleted"

        // btnUndo functionality
        val btnUndo: Button = snackbarLayout.findViewById(R.id.btnSnackbar)
        btnUndo.text = "undo"
        btnUndo.setOnClickListener {
            cancelDeleteTask(deletedTaskItem, pos)
            snack.dismiss()
        }

        snack.show()
    }

    private fun cancelDeleteTask(deletedTaskItem: TaskItem, pos: Int) {
        if (deletedTask != null) {
            // restore Task (back end)
            viewModel.restoreTask(deletedTask!!) // update ViewModel
            doneList = viewModel.getDoneList()

            // restore TaskItem (front end)
            consolidatedList.add(pos, deletedTaskItem)
            rvAdapter.notifyItemInserted(pos)
            checkForEmptyList()

            // enable fab in LogFragment
            setFragmentResult("rqCheckFabClickability", bundleOf())

        } else {
            FancyToast.makeText(requireContext(), "Failed to restore task", FancyToast.LENGTH_SHORT, FancyToast.DEFAULT, false).show()
        }
    }

    private fun createRV() {
        rvDone = binding.rvDone
        rvAdapter = RVAdapterLog(consolidatedList, mapSubjectColor, colors, glow, bars)

        // set adapter to recycler view
        rvDone.adapter = rvAdapter

        swipeFunctions()

        // item click listener
        rvAdapter.setOnItemClickListener(object: RVAdapterLog.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // do nothing
            }

        })
        rvAdapter.notifyDataSetChanged()

        // filter list (search function)
        setFragmentResultListener("filterDoneList") { _, bundle ->
            val filteredList: ArrayList<Task> = bundle.getParcelableArrayList<Task>("filteredList") as ArrayList<Task>
            val newConsolidatedList = viewModel.createFilteredConsolidatedDoneList(filteredList)
            rvAdapter.filterList(newConsolidatedList)

            // update lists
            doneList = filteredList
            consolidatedList = newConsolidatedList
        }
    }

    private fun deleteTask(pos: Int) {
        deletedTask = doneList[pos] // save as deletedTask in case user wants to restore it

        doneList.removeAt(pos)
        viewModel.saveJsonTaskLists()
        val bundle = Bundle()
        bundle.putInt("fabClickability", 0)
        setFragmentResult("rqCheckFabClickability", bundle)
        setFragmentResult("listsChanged", bundleOf()) // update FragmentLog

        checkForEmptyList() // displays "No completed tasks" if list is empty
    }

    private fun markAsUndone(undoneTask: Task) {
        viewModel.markAsUndone(undoneTask)

        // get LogFragment to call checkFabClickability() when a task is marked as undone
        val bundle = Bundle()
        bundle.putInt("fabClickability", 0)
        setFragmentResult("rqCheckFabClickability", bundle)
    }

    private fun getLists() {
        doneList = viewModel.getDoneList()
        colors = viewModel.getColors()

        if (firstInstance) { // first time initializing DoneFragment
            consolidatedList = viewModel.getConsolidatedListDone()
            mapSubjectColor = viewModel.getMapSubjectColor()
            createRV()
            firstInstance = false

        } else {
            val newConsolidatedList = viewModel.getConsolidatedListDone()
            val newMapSubjectColor = viewModel.getMapSubjectColor()
            if (newConsolidatedList != consolidatedList || newMapSubjectColor != mapSubjectColor) { // update list & map if they've been changed
                consolidatedList = newConsolidatedList
                mapSubjectColor = newMapSubjectColor
                createRV() // re-create RV only if data's been changed
            }
        }
    }

    private fun checkClearAll() {
        setFragmentResultListener("rqClearAll") { _, _ ->
            getLists()
            checkForEmptyList()
        }
    }

    private fun getSettings() {
        listSettingsItems = viewModel.getListSettings()
        bars = intToBoolean(listSettingsItems[2].option)
        glow = intToBoolean(listSettingsItems[3].option)
        // TODO: future user preferences here //
    }

    private fun intToBoolean(option: Int): Boolean {
        return option != 0
    }
}