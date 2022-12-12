package com.example.logit

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

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
        val switchSettings: SwitchCompat = itemView.findViewById(R.id.switchSettings)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views with data from list
        holder.tvSettingsItem.text = listSettingsItems[position].item

        when (position) {
            0 -> { // task items colored shadow
                holder.switchSettings.apply {
                    visibility = View.VISIBLE
                    isChecked = listSettingsItems[position].status // set switch status based on saved preferences
                    // implement listener
                    setOnCheckedChangeListener { _, isChecked ->
                        (context as ActivityAllSettings).updateSettings(position, isChecked) // changes settings item status to match switch checked / unchecked
                    }
                }
            }

            1 -> { // header bars
                holder.switchSettings.apply {
                    visibility = View.VISIBLE
                    isChecked = listSettingsItems[position].status // set switch status based on saved preferences
                    // implement listener
                    setOnCheckedChangeListener { _, isChecked ->
                        (context as ActivityAllSettings).updateSettings(position, isChecked) // changes settings item status to match switch checked / unchecked
                    }
                }
            }

            2 -> { // subject color codes
                holder.linearLayout.setOnClickListener {
                    val context = holder.linearLayout.context
                    val intent = Intent(context, ActivitySettingsColorCodes::class.java)

                    (context as ActivityAllSettings).startActivity(intent)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return listSettingsItems.size
    }
}
