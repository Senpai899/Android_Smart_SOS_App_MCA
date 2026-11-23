package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.Helpline
import com.google.android.material.button.MaterialButton

class HelplinesAdapter(
    private var helplines: List<Helpline>,
    private val onCallClick: (Helpline) -> Unit
) : RecyclerView.Adapter<HelplinesAdapter.HelplineViewHolder>() {

    class HelplineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategoryBadge)
        val tvTitle: TextView = itemView.findViewById(R.id.tvHelplineTitle)
        val tvNumber: TextView = itemView.findViewById(R.id.tvHelplineNumber)
        val tvDesc: TextView = itemView.findViewById(R.id.tvHelplineDesc)
        val btnCall: MaterialButton = itemView.findViewById(R.id.btnCallHelpline)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HelplineViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_helpline, parent, false)
        return HelplineViewHolder(view)
    }

    override fun onBindViewHolder(holder: HelplineViewHolder, position: Int) {
        val item = helplines[position]
        holder.tvCategory.text = item.category
        holder.tvTitle.text = item.title
        holder.tvNumber.text = item.number
        holder.tvDesc.text = item.description
        holder.btnCall.setOnClickListener { onCallClick(item) }
    }

    override fun getItemCount(): Int = helplines.size

    fun updateData(newList: List<Helpline>) {
        this.helplines = newList
        notifyDataSetChanged()
    }
}