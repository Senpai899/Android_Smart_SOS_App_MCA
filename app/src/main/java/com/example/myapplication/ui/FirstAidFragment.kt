package com.example.myapplication.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.FirstAidRepository
import com.example.myapplication.model.FirstAidGuide

class FirstAidFragment : Fragment() {

    private lateinit var rvFirstAid: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: FirstAidAdapter
    private var allGuides = emptyList<FirstAidGuide>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_first_aid, container, false)
        rvFirstAid = view.findViewById(R.id.rvFirstAid)
        etSearch = view.findViewById(R.id.etSearchFirstAid)

        allGuides = FirstAidRepository.getGuides()
        setupRecyclerView()
        setupSearchFilter()

        return view
    }

    private fun setupRecyclerView() {
        adapter = FirstAidAdapter(guides = allGuides)
        rvFirstAid.layoutManager = LinearLayoutManager(requireContext())
        rvFirstAid.adapter = adapter
    }

    private fun setupSearchFilter() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterGuides(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterGuides(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allGuides)
            return
        }
        val filtered = allGuides.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.symptoms.contains(query, ignoreCase = true) ||
            it.emergencySteps.any { step -> step.contains(query, ignoreCase = true) }
        }
        adapter.updateData(filtered)
    }
}