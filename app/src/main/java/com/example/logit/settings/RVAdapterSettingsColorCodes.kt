package com.example.logit.settings

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.logit.R
import com.example.logit.log.SubjectColor

class RVAdapterSettingsColorCodes(
    private val listSubjectColor: ArrayList<SubjectColor>, // list of items to populate rv with
    private val listColors: ArrayList<Int>
) : RecyclerView.Adapter<RVAdapterSettingsColorCodes.NewViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): NewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.rv_item_settings_color_codes,
            parent,
            false
        )

        return NewViewHolder(itemView, listColors)
    }

    class NewViewHolder(itemView: View, listColors: ArrayList<Int>) : RecyclerView.ViewHolder(itemView) { // initialize views

        val cardView: CardView = itemView.findViewById(R.id.cardView)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)
        val etSubject: EditText = itemView.findViewById(R.id.etSubject)

        val context = etSubject.context

        val spinnerColor: Spinner = itemView.findViewById(R.id.spinnerColor)
        val adapter = SpinnerAdapterSettings(context, listColors)
    }

    override fun onBindViewHolder(
        holder: NewViewHolder,
        position: Int
    ) { // populate views with data from list

        holder.setIsRecyclable(false) // prevent "recycling" the views - keeps EditText content/position from being jumbled up

        val subject = listSubjectColor[position].subject
        val colorIndex = listSubjectColor[position].colorIndex

        holder.etSubject.setText(subject)
        holder.etSubject.addTextChangedListener(
            textWatcher(
                holder.etSubject,
                position
            )
        ) // to watch for duplicate subject entries

        holder.spinnerColor.apply {
            adapter = holder.adapter
            setSelection(colorIndex)
            onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.context, listColors[p2])) // syncs tab color with the selected spinner color
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    // do nothing
                }

            }
        }

        // set colors
        holder.linearLayout.setBackgroundColor(getColor(holder.linearLayout.context, com.google.android.material.R.attr.colorPrimaryContainer))
        holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.cardView.context, listColors[colorIndex]))
    }

    override fun getItemCount(): Int {
        return listSubjectColor.size
    }

    class textWatcher(val etSubject: EditText, val position: Int) : TextWatcher {

        val context = etSubject.context

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(p0: Editable?) {
            val input = p0.toString().trim()

            (context as ColorCodesSettingsActivity).updateListSubject(input, position) // update listSubject to match changes

            (context as ColorCodesSettingsActivity).checkDuplicates() // checkDuplicates() is run in MainActivity so it can iterate through all items in rvList
        }
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun getColor(context: Context, colorResId: Int): Int {
        val typedValue = TypedValue()
        val typedArray = context.obtainStyledAttributes(typedValue.data, intArrayOf(colorResId))
        val color = typedArray.getColor(0, 0)
        typedArray.recycle()
        return color
    }
}
