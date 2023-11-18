package es.emmanuel.logit.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.logit.R
import com.example.logit.databinding.FragmentSettingsBinding
import es.emmanuel.logit.ParentActivity
import es.emmanuel.logit.ViewModelParent

class SettingsFragment : Fragment() {

    lateinit var viewModel: ViewModelParent
    lateinit var listSettingsItems: ArrayList<SettingsItem>

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(requireActivity())[ViewModelParent::class.java]

        // get existing settings
        listSettingsItems = viewModel.getListSettings()
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

        syncSettings()
    }

    private fun syncSettings() {
        // TODO: future user preferences here //
        // GENERAL SETTINGS
        // edit color codes
        binding.generalSettings.editColorCodes.setOnClickListener {
            val intent = Intent(requireContext(), ColorCodesSettingsActivity::class.java)
            (context as ParentActivity).startActivity(intent)
        }
        // automatically delete completed tasks
        val adapter = ArrayAdapter.createFromResource(requireContext(), R.array.auto_delete_options, android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.generalSettings.spinnerAutoDelete.adapter = adapter
        val selectedOption: Int = listSettingsItems[1].option
        binding.generalSettings.spinnerAutoDelete.apply {
            setSelection(selectedOption)
            onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    p0: AdapterView<*>?,
                    p1: View?,
                    p2: Int,
                    p3: Long
                ) {
                    (context as ParentActivity).viewModel.updateSettings(1, p2)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // do nothing
                }
            }
        }

        // LOG SETTINGS
        // header bars
        binding.logSettings.switchHeaderBars.apply {
            isChecked = intToBoolean(listSettingsItems[2].option)
            setOnCheckedChangeListener { _, isChecked ->
                val option = booleanToInt(isChecked)
                (context as ParentActivity).viewModel.updateSettings(2, option)
            }
        }
        // background glow
        binding.logSettings.switchBackgroundGlow.apply {
            isChecked = intToBoolean(listSettingsItems[3].option)
            setOnCheckedChangeListener { _, isChecked ->
                val option = booleanToInt(isChecked)
                (context as ParentActivity).viewModel.updateSettings(3, option)
            }
        }

        // CALENDAR SETTINGS
        // show completed tasks
        binding.calendarSettings.switchShowCompletedTasks.apply {
            isChecked = intToBoolean(listSettingsItems[4].option)
            setOnCheckedChangeListener { _, isChecked ->
                val option = booleanToInt(isChecked)
                (context as ParentActivity).viewModel.updateSettings(4, option)
            }
        }
    }

    private fun booleanToInt(status: Boolean): Int {
        return if (!status) {
            0
        } else {
            1
        }
    }

    private fun intToBoolean(option: Int): Boolean {
        return option != 0
    }
}