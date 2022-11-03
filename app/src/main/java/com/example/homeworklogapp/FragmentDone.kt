package com.example.homeworklogapp

import android.R
import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.homeworklogapp.databinding.FragmentDoneBinding


class FragmentDone : Fragment() {

    lateinit var RVDone: RecyclerView
    lateinit var mainLogRVAdapter: MainLogRVAdapter
    lateinit var doneList: ArrayList<Task>

    private var _binding: FragmentDoneBinding? = null

    lateinit var mapSubjectColor: HashMap<String, Int>

    lateinit var listColors: ArrayList<Color>

    // setup view binding
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentDoneBinding.inflate(inflater, container, false)
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
                mainLogRVAdapter.notifyItemRemoved(position)

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
                mainLogRVAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                confirmDelete(deletedTask, position)
            }
            // at last we are adding this
            // to our recycler view.
        }).attachToRecyclerView(RVDone)
    }

    private fun createRV() {
        RVDone = binding.rvDone
        mainLogRVAdapter = MainLogRVAdapter(doneList, mapSubjectColor, listColors)

        // set adapter to recycler view
        RVDone.adapter = mainLogRVAdapter

        swipeFunctions()

        // item click listener
        mainLogRVAdapter.setOnItemClickListener(object: MainLogRVAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {
                // do nothing
            }

        })

        mainLogRVAdapter.notifyDataSetChanged()
    }

    private fun restoreTask(restoredTask: Task) {
        setFragmentResult("rqRestoredTask", bundleOf("bundleRestoredTask" to restoredTask))
    }

    private fun confirmDelete(deletedTask: Task, position: Int) {
        var touched = false

        // alert dialog
        val alertDialog: AlertDialog = requireContext().let {
            val builder = AlertDialog.Builder(it)
            builder.apply {
                setPositiveButton("Confirm"
                ) { dialog, id ->
                    deleteTask(deletedTask)
                    touched = true
                }

                setNegativeButton("Cancel"
                ) { dialog, id ->
                    doneList.add(position, deletedTask)
                    mainLogRVAdapter.notifyItemInserted(position)
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
                mainLogRVAdapter.notifyItemInserted(position)
            }
        }
    }

    private fun deleteTask(deletedTask: Task) {
        setFragmentResult("rqDeletedTask", bundleOf("bundleDeletedTask" to deletedTask))
    }

    private fun getColor(context: Context, colorResId: Int): Int {

        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun getFromBundle() {
        setFragmentResultListener("rqDoneList") { requestKey, bundle ->
            doneList = bundle.getParcelableArrayList("doneList")!!

            setFragmentResultListener("rqMapSubjectColorDone") { requestKey, bundle ->
                mapSubjectColor = bundle.getSerializable("mapSubjectColor")!! as HashMap<String, Int>
                createRV()
            }
        }

        // retrieve listColors
        setFragmentResultListener("rqListColorsDone") { requestKey, bundle ->
            listColors = bundle.getParcelableArrayList("listColors")!!
        }
    }
}