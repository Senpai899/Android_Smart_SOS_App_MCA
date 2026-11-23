package com.example.myapplication.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.model.FirstAidGuide

class FirstAidAdapter(
    private var guides: List<FirstAidGuide>
) : RecyclerView.Adapter<FirstAidAdapter.GuideViewHolder>() {

    private val expandedPositions = mutableSetOf<Int>()

    class GuideViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tvGuideTitle)
        val tvCategory: TextView = itemView.findViewById(R.id.tvGuideCategory)
        val tvExpandIndicator: TextView = itemView.findViewById(R.id.tvExpandIndicator)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)
        val tvSymptoms: TextView = itemView.findViewById(R.id.tvSymptoms)
        val tvSteps: TextView = itemView.findViewById(R.id.tvSteps)
        val tvDonts: TextView = itemView.findViewById(R.id.tvDonts)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuideViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_first_aid, parent, false)
        return GuideViewHolder(view)
    }

    override fun onBindViewHolder(holder: GuideViewHolder, position: Int) {
        val guide = guides[position]
        holder.tvTitle.text = guide.title
        holder.tvCategory.text = guide.category
        holder.tvSymptoms.text = guide.symptoms
        holder.tvSteps.text = guide.emergencySteps.joinToString("\n")
        holder.tvDonts.text = guide.donts.joinToString("\n")

        val isExpanded = expandedPositions.contains(position)
        holder.layoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE
        holder.tvExpandIndicator.text = if (isExpanded) "▲" else "▼"

        holder.itemView.setOnClickListener {
            if (isExpanded) {
                expandedPositions.remove(position)
            } else {
                expandedPositions.add(position)
            }
            notifyItemChanged(position)
        }
    }

    override fun getItemCount(): Int = guides.size

    fun updateData(newList: List<FirstAidGuide>) {
        this.guides = newList
        expandedPositions.clear()
        notifyDataSetChanged()
    }
}