package com.example.logit.log

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.logit.R
import com.example.logit.Task
import com.example.logit.ViewModelParent
import com.example.logit.addtask.AddTaskActivity
import com.example.logit.databinding.FragmentLogBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class LogFragment : Fragment() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var fabTask: FloatingActionButton
    lateinit var todoList: ArrayList<Task>
    lateinit var doneList: ArrayList<Task>
    var currentFrag = 0 // 0 = FragmentTodo, 1 = FragmentDone

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModelParent

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]

        // initialize lists
        listCardColors = viewModel.getListCardColors()
        listSubjectColor = viewModel.getListSubjectColor()
        todoList = viewModel.getTodoList()
        doneList = viewModel.getDoneList()

        // updates lists when user swipes in FragmentTodo and FragmentDone
        childFragmentManager.setFragmentResultListener("listsChanged", this) { _, _ ->
            todoList = viewModel.getTodoList()
            doneList = viewModel.getDoneList()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().title = ""

        // fab
        fabTask = binding.fabTask
        fabTask.setOnClickListener {
            startActivityAddTask()
        }

        // show/hide fab on user scroll
        childFragmentManager.setFragmentResultListener("showFab", this) { _, _ ->
            showFabAddTask()
        }
        childFragmentManager.setFragmentResultListener("hideFab", this) { _, _ ->
            hideFabAddTask()
        }

        // tab layout
        tabLayout = binding.tabLayout
        viewPager = binding.viewPager
        val tabTitles = listOf("to do", "done")
        val adapter = TabLayoutAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = tabTitles[position]
        }.attach()
        tabLayout.addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position
                if (position == 0) { // in fragmentTodo
                    currentFrag = 0
                    enableFab()
                    fabTask.setImageResource(android.R.drawable.ic_input_add)
                    fabTask.setOnClickListener {
                        startActivityAddTask()
                    }
                } else { // in fragmentDone
                    currentFrag = 1
                    fabTask.setImageResource(R.drawable.ic_delete)
                    checkFabClickability() // set clickability
                    fabTask.setOnClickListener {
                        confirmClearAll()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                fabTask.setOnClickListener(null) // removes click listener
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })

        // set fab enabled / disabled when task is deleted or marked as undone
        childFragmentManager.setFragmentResultListener("rqCheckFabClickability", requireActivity()) { _, _ ->
            checkFabClickability()
        }

        // menu
        val menuHost = requireActivity()
        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_log_menu, menu)
                val searchItem: MenuItem = menu.findItem(R.id.actionSearch)
                val searchView: SearchView = searchItem.actionView as SearchView
                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        // updates lists before filtering
                        childFragmentManager.setFragmentResultListener("listsChanged", this@LogFragment) { _, _ ->
                            todoList = viewModel.getTodoList()
                            doneList = viewModel.getDoneList()
                        }

                        filter(p0)
                        return false
                    }
                    private fun filter(p0: String?) {
                        val filteredList: ArrayList<Task> = arrayListOf()
                        if (p0 != null) {
                            if (currentFrag == 0) { // fragmentTodo
                                if (todoList.isNotEmpty()) {
                                    for (task in todoList) {
                                        if (task.subject.contains(p0, true) || task.task.contains(p0, true)) {
                                            filteredList.add(task)
                                        }
                                    }
                                }
                                childFragmentManager.setFragmentResult("filterTodoList", bundleOf("filteredList" to filteredList))

                            } else { // fragmentDone
                                if (doneList.isNotEmpty()) {
                                    for (task in doneList) {
                                        if (task.subject.contains(p0, true) || task.task.contains(p0, true)) {
                                            filteredList.add(task)
                                        }
                                    }
                                }
                                childFragmentManager.setFragmentResult("filterDoneList", bundleOf("filteredList" to filteredList))
                            }
                        }
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.actionSearch -> {
                        true
                    } else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // without this line, there will be duplicates of search icon when i return to this fragment
    }

    private fun confirmClearAll() {
        if (doneList.size > 0) {
            // alert dialog
            val builder = AlertDialog.Builder(requireContext()).create()
            if (builder.window != null) { // set default background to transparent
                builder.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            }
            val view = layoutInflater.inflate(R.layout.alert_dialog_custom, null, false)
            val tvPrimary: TextView = view.findViewById(R.id.tvPrimary)
            val tvSecondary: TextView = view.findViewById(R.id.tvSecondary)
            val btnCancel: Button = view.findViewById(R.id.btnCancel)
            val btnConfirm: Button = view.findViewById(R.id.btnConfirm)

            tvPrimary.text = getString(R.string.confirm_delete_primary)
            tvSecondary.text = getString(R.string.confirm_delete_secondary)

            btnCancel.setOnClickListener {
                builder.dismiss()
            }
            btnConfirm.setOnClickListener {
                deleteAll()
                builder.dismiss()
            }

            builder.apply {
                setView(view)
                setCanceledOnTouchOutside(true)
                show()
            }
        } else {
            Snackbar.make(requireContext(), binding.tabLayout, "No tasks to clear", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun deleteAll() { // delete all items in doneList
        viewModel.clearDoneList()
        doneList = viewModel.getDoneList()

        // update fragmentDone
        val bundle = Bundle()
        bundle.putInt("clearAll", 0)
        childFragmentManager.setFragmentResult("rqClearAll", bundle)

        disableFab()
    }

    private fun checkFabClickability() {
        // faded fab and unclickable when doneList is empty
        doneList = viewModel.getDoneList()

        if (doneList.size > 0) {
            enableFab()
        } else {
            disableFab()
        }
    }

    private fun disableFab() {
        fabTask.isEnabled = false
        fabTask.background.alpha = 45
    }

    private fun enableFab() {
        fabTask.isEnabled = true
        fabTask.background.alpha = 255
    }

    private fun startActivityAddTask() {
        val intent = Intent(requireContext(), AddTaskActivity::class.java)

        val listSubjects: ArrayList<String> = viewModel.getListSubjects()
        intent.putExtra("listSubjects", listSubjects)
        startActivity(intent)
    }

    private fun showFabAddTask() { // called when user scrolls up
        fabTask.show()
    }

    private fun hideFabAddTask() { // called when user scrolls down
        fabTask.hide()
    }
}