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
import kotlinx.android.synthetic.main.fragment_done.*


class FragmentDone : Fragment() {

    lateinit var RVDone: RecyclerView
    lateinit var RVAdapter: RVAdapterMain
    lateinit var doneList: ArrayList<Task>
    lateinit var doneItemList: List<ListItem>

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

        // restore task
        ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val restoredTask = doneList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition

                doneList.removeAt(position)
                RVAdapter.notifyItemRemoved(position)

                restoreTask(restoredTask)
            }
        }).attachToRecyclerView(RVDone)

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

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // change task status
                val deletedTask: Task = doneList[viewHolder.adapterPosition]
                val position = viewHolder.adapterPosition

                // this method is called when item is swiped.
                // below line is to remove item from our array list.
                doneList.removeAt(viewHolder.adapterPosition)

                // below line is to notify our item is removed from adapter.
                RVAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                confirmDelete(deletedTask, position)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RVDone)
    }

    private fun createRV() {
        RVDone = binding.rvDone
        RVAdapter = RVAdapterMain(doneItemList, mapSubjectColor, listCardColors, glow)

        // set adapter to recycler view
        RVDone.adapter = RVAdapter

        swipeFunctions()

        // item click listener
        RVAdapter.setOnItemClickListener(object: RVAdapterMain.OnItemClickListener {
            override fun onItemClick(position: Int) {
                // do nothing
            }

        })

        RVAdapter.notifyDataSetChanged()
    }

    private fun confirmDelete(deletedTask: Task, position: Int) {
        var touched = false

        // alert dialog
        val alertDialog: AlertDialog = requireContext().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Confirm"
                ) { dialog, id ->
                    deleteTask()
                    touched = true
                }

                setNegativeButton("Cancel"
                ) { dialog, id ->
                    doneList.add(position, deletedTask)
                    RVAdapter.notifyItemInserted(position)
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
                doneList.add(position, deletedTask)
                RVAdapter.notifyItemInserted(position)
            }
        }
    }

    private fun deleteTask() {
        val bundle = Bundle()
        bundle.putInt("fabClickability", 0)
        setFragmentResult("rqCheckFabClickability", bundle)
    }

    private fun restoreTask(restoredTask: Task) {
        viewModel.restoreTask(restoredTask)

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
        doneItemList = viewModel.getDoneItemList()
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