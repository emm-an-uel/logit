package es.emmanuel.logit.log

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
import com.example.logit.databinding.FragmentLogBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import es.emmanuel.logit.Task
import es.emmanuel.logit.ViewModelParent
import es.emmanuel.logit.addtask.AddTaskActivity

class LogFragment : Fragment() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var fabTask: FloatingActionButton
    lateinit var todoList: ArrayList<Task>
    lateinit var doneList: ArrayList<Task>
    private lateinit var originalTodoList: ArrayList<Task>
    private lateinit var originalDoneList: ArrayList<Task>
    var currentFrag = 0 // 0 = FragmentTodo, 1 = FragmentDone

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    private lateinit var colors: ArrayList<Int>

    lateinit var viewModel: ViewModelParent

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]
        getData()

        // updates lists when user swipes in FragmentTodo and FragmentDone
        childFragmentManager.setFragmentResultListener("listsChanged", this) { _, _ ->
            todoList = viewModel.getTodoList()
            doneList = viewModel.getDoneList()
            originalTodoList = viewModel.getTodoList()
            originalDoneList = viewModel.getDoneList()
        }
    }

    override fun onResume() {
        super.onResume()
        getData()
    }

    private fun getData() {
        // initialize lists
        colors = viewModel.getColors()
        listSubjectColor = viewModel.getListSubjectColor()
        todoList = viewModel.getTodoList()
        doneList = viewModel.getDoneList()
        originalTodoList = viewModel.getTodoList()
        originalDoneList = viewModel.getDoneList()
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

        // fab
        fabTask = binding.fabTask
        fabTask.setOnClickListener {
            startActivityAddTask()
        }

        // show/hide fab on user scroll
        childFragmentManager.setFragmentResultListener("showFab", this) { _, _ ->
            fabTask.show()
        }
        childFragmentManager.setFragmentResultListener("hideFab", this) { _, _ ->
            fabTask.hide()
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
        createMenu()
    }

    private fun createMenu() {
        requireActivity().addMenuProvider(object: MenuProvider {
            // TODO: hide fab when searching
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_log_menu, menu)
                val searchItem = menu.findItem(R.id.actionSearch)
                val searchView: SearchView = searchItem.actionView as SearchView
                searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener {
                    override fun onQueryTextSubmit(p0: String?): Boolean {
                        return false
                    }

                    override fun onQueryTextChange(p0: String?): Boolean {
                        filter(p0)
                        return false
                    }

                    private fun filter(p0: String?) {
                        val filteredList: ArrayList<Task> = arrayListOf()
                        if (p0 != null) {
                            if (currentFrag == 0) { // TodoFragment

                                if (p0 == "") { // if search is empty
                                    todoList = originalTodoList

                                } else { // if search is not empty
                                    for (task in todoList) {
                                        if (task.subject.contains(p0, true) || (task.task.contains(p0, true))) {
                                            filteredList.add(task)
                                        }
                                    }
                                    todoList = filteredList
                                }
                                childFragmentManager.setFragmentResult("filterTodoList", bundleOf("filteredList" to todoList))

                            } else { // DoneFragment
                                if (p0 == "") { // if search is empty
                                    doneList = originalDoneList

                                } else { // if search is not empty
                                    for (task in doneList) {
                                        if (task.subject.contains(p0, true) || (task.task.contains(p0, true))) {
                                            filteredList.add(task)
                                        }
                                    }
                                    doneList = filteredList
                                }
                                childFragmentManager.setFragmentResult("filterDoneList", bundleOf("filteredList" to doneList))
                            }
                        }
                    }
                })
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.actionSearch -> true
                    else -> false
                }
            }

        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
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
            btnConfirm.text = "Delete"
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
}