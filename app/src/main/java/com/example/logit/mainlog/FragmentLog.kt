package com.example.logit.mainlog

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.*
import android.widget.SearchView
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.example.logit.R
import com.example.logit.ViewModelParent
import com.example.logit.addtask.ActivityAddTask
import com.example.logit.databinding.FragmentLogBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class FragmentLog : Fragment() {

    lateinit var tabLayout: TabLayout
    lateinit var viewPager: ViewPager2
    lateinit var fabTask: FloatingActionButton
    lateinit var doneList: ArrayList<Task>

    lateinit var listSubjectColor: ArrayList<SubjectColor>

    lateinit var listCardColors: ArrayList<CardColor>

    lateinit var viewModel: ViewModelParent

    private var _binding: FragmentLogBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requireActivity().title = ""
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]

        // initialize lists
        listCardColors = viewModel.getListCardColors()
        listSubjectColor = viewModel.getListSubjectColor()
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
                    fabEnabled()
                    fabTask.setImageResource(android.R.drawable.ic_input_add)
                    fabTask.setOnClickListener {
                        startActivityAddTask()
                    }
                } else {
                    fabTask.setImageResource(R.drawable.icon_trash)
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
        remoteCheckFabClickability()

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
                        filter(p0)
                        return false
                    }
                    private fun filter(p0: String?) {
                        val filteredList: ArrayList<ListItem> = arrayListOf()
                        // TODO: filter logic
                        childFragmentManager.setFragmentResult("filterList", bundleOf("filteredList" to filteredList))
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

        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // without this line, there will be duplicates of settings icon when i return to this fragment
    }

    override fun onResume() {
        super.onResume()
        viewModel.apply {
            initListSettings()
            initTaskLists()
            createConsolidatedListTodo()
            createConsolidatedListDone()
            initSubjectColor()
        }
    }

    private fun remoteCheckFabClickability() {
        parentFragmentManager.setFragmentResultListener("rqCheckFabClickability", requireActivity()) { _, _ ->
            checkFabClickability()
        }
    }

    private fun confirmClearAll() {
        if (doneList.size > 0) {
            // alert dialog
            val alertDialog: AlertDialog = requireContext().let {
                val builder = AlertDialog.Builder(it)
                builder.apply {
                    setPositiveButton(
                        "Confirm"
                    ) { dialog, id ->
                        clearAll()
                    }

                    setNegativeButton(
                        "Cancel"
                    ) { dialog, id ->
                        // do nothing
                    }
                }
                builder.setMessage("Clear all tasks?")
                builder.create()
            }
            alertDialog.show()
            val actualColorAccent = getColor(requireContext(), android.R.attr.colorAccent)
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(actualColorAccent)
            alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(actualColorAccent)
        } else {
            Snackbar.make(requireContext(), binding.tabLayout, "No tasks to clear", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }

    private fun clearAll() { // delete all items in doneList
        viewModel.clearDoneList()
        doneList = viewModel.getDoneList()

        // update fragmentDone
        val bundle = Bundle()
        bundle.putInt("clearAll", 0)
        parentFragmentManager.setFragmentResult("rqClearAll", bundle)

        fabDisabled()
    }

    private fun checkFabClickability() {
        // faded fab and unclickable when doneList is empty
        doneList = viewModel.getDoneList()

        if (doneList.size > 0) {
            fabEnabled()
        } else {
            fabDisabled()
        }
    }

    private fun fabDisabled() {
        fabTask.isEnabled = false
        fabTask.background.alpha = 45
    }

    private fun fabEnabled() {
        fabTask.isEnabled = true
        fabTask.background.alpha = 255
    }

    private fun startActivityAddTask() {
        val intent = Intent(requireContext(), ActivityAddTask::class.java)

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