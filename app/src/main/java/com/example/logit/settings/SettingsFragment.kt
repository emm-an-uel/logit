package com.example.logit.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.ViewModelParent
import com.example.logit.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    lateinit var viewModel: ViewModelParent
    lateinit var rv: RecyclerView
    lateinit var rvAdapter: RVAdapterAllSettings
    lateinit var listSettingsItems: ArrayList<SettingsItem>

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]

        // get existing settings OR start from scratch if none saved
        listSettingsItems = viewModel.getListSettings()
        if (listSettingsItems.size == 0) {
            listSettingsItems.apply {
                add(SettingsItem("Background glow", 0))
                add(SettingsItem("Header bars", 0))
                add(SettingsItem("Automatically delete completed tasks", 3)) // '30 days' by default
                add(SettingsItem("Edit subject color codes", 0))
                // TODO: add additional settings items here //
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRVSettings()
    }

    private fun setupRVSettings() {
        rv = binding.rvSettings
        rvAdapter = RVAdapterAllSettings(listSettingsItems)
        rv.adapter = rvAdapter
        rv.addItemDecoration(DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL))
    }
}