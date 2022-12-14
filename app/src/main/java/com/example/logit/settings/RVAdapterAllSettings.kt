package com.example.logit.settings

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.ParentActivity
import com.example.logit.R

class RVAdapterAllSettings(
    private val listSettingsItems: ArrayList<SettingsItem>
) : RecyclerView.Adapter<RVAdapterAllSettings.NewViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewViewHolder { // inflate the layout for task_rv_item.xml
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_item_all_settings,
            parent, false
        )

        return NewViewHolder(itemView)
    }

    class NewViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) { // initialize views
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        val tvSettingsItem: TextView = itemView.findViewById(R.id.tvSettingsItem)
        val switch: SwitchCompat = itemView.findViewById(R.id.switchSettings)
        val spinner: Spinner = itemView.findViewById(R.id.spinnerSettings)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views with data from list
        holder.tvSettingsItem.text = listSettingsItems[position].item
        val context = holder.tvSettingsItem.context

        val pos = holder.adapterPosition

        when (position) {
            0 -> { // task items colored shadow
                val status = intToBoolean(listSettingsItems[position].option) // convert int to boolean

                holder.switch.apply {
                    visibility = View.VISIBLE
                    isChecked = status // set switch status based on saved preferences
                    // implement listener
                    setOnCheckedChangeListener { _, isChecked ->
                        val option = booleanToInt(isChecked) // convert boolean to int
                        (context as ParentActivity).updateSettings(pos, option) // changes settings item status to match switch checked / unchecked
                    }
                }
            }

            1 -> { // header bars
                val status = intToBoolean(listSettingsItems[pos].option) // convert int to boolean

                holder.switch.apply {
                    visibility = View.VISIBLE
                    isChecked = status // set switch status based on saved preferences
                    // implement listener
                    setOnCheckedChangeListener { _, isChecked ->
                        val option = booleanToInt(isChecked) // convert boolean to int
                        (context as ParentActivity).updateSettings(position, option) // changes settings item status to match switch checked / unchecked
                    }
                }
            }

            2 -> { // automatically delete completed tasks
                val options = R.array.auto_delete_options
                val adapter = ArrayAdapter.createFromResource(context, options, android.R.layout.simple_spinner_item)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                holder.spinner.adapter = adapter

                val selectedOption: Int = listSettingsItems[pos].option
                holder.spinner.apply {
                    visibility = View.VISIBLE
                    setSelection(selectedOption) // sets selection to what was saved
                    onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(
                            p0: AdapterView<*>?,
                            p1: View?,
                            p2: Int,
                            p3: Long
                        ) {
                            (context as ParentActivity).updateSettings(pos, p2)
                        }

                        override fun onNothingSelected(p0: AdapterView<*>?) {
                            // do nothing
                        }
                    }
                }
            }

            3 -> { // subject color codes
                holder.linearLayout.setOnClickListener {
                    val intent = Intent(context, ColorCodesSettingsActivity::class.java)
                    (context as ParentActivity).startActivity(intent)
                }
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

    override fun getItemCount(): Int {
        return listSettingsItems.size
    }
}
