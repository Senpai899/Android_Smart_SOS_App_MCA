package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
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
import com.example.myapplication.data.HelplinesRepository
import com.example.myapplication.model.Helpline

class HelplinesFragment : Fragment() {

    private lateinit var rvHelplines: RecyclerView
    private lateinit var etSearch: EditText
    private lateinit var adapter: HelplinesAdapter
    private var allHelplines = emptyList<Helpline>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_helplines, container, false)
        rvHelplines = view.findViewById(R.id.rvHelplines)
        etSearch = view.findViewById(R.id.etSearchHelplines)

        allHelplines = HelplinesRepository.getIndianHelplines()
        setupRecyclerView()
        setupSearchFilter()

        return view
    }

    private fun setupRecyclerView() {
        adapter = HelplinesAdapter(
            helplines = allHelplines,
            onCallClick = { helpline ->
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${helpline.number}"))
                startActivity(intent)
            }
        )
        rvHelplines.layoutManager = LinearLayoutManager(requireContext())
        rvHelplines.adapter = adapter
    }

    private fun setupSearchFilter() {
        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterHelplines(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterHelplines(query: String) {
        if (query.isEmpty()) {
            adapter.updateData(allHelplines)
            return
        }
        val filtered = allHelplines.filter {
            it.title.contains(query, ignoreCase = true) ||
            it.number.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true)
        }
        adapter.updateData(filtered)
    }
}