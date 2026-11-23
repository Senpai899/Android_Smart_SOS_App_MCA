package com.example.myapplication.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.model.Contact
import com.example.myapplication.utils.ContactValidator
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputLayout

class ContactsFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var rvContacts: RecyclerView
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var btnAddFirst: MaterialButton
    private lateinit var adapter: ContactsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)
        prefs = PreferencesManager(requireContext())

        rvContacts = view.findViewById(R.id.rvContacts)
        layoutEmpty = view.findViewById(R.id.layoutEmptyContacts)
        fabAdd = view.findViewById(R.id.fabAddContact)
        btnAddFirst = view.findViewById(R.id.btnAddFirstContact)

        setupRecyclerView()

        fabAdd.setOnClickListener { showAddContactDialog() }
        btnAddFirst.setOnClickListener { showAddContactDialog() }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
    }

    private fun setupRecyclerView() {
        adapter = ContactsAdapter(
            contacts = emptyList(),
            onVerifyClick = { contact ->
                checkAndVerifyContact(contact)
            },
            onCallClick = { contact ->
                makePhoneCall(contact.phoneNumber)
            },
            onDeleteClick = { contact ->
                confirmDeleteContact(contact)
            }
        )
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = adapter
    }

    private fun loadContacts() {
        val contacts = prefs.getContacts()
        if (contacts.isEmpty()) {
            layoutEmpty.visibility = View.VISIBLE
            rvContacts.visibility = View.GONE
        } else {
            layoutEmpty.visibility = View.GONE
            rvContacts.visibility = View.VISIBLE
            adapter.updateData(contacts)
        }
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_contact, null)
        val tilName = dialogView.findViewById<TextInputLayout>(R.id.tilContactName)
        val tilPhone = dialogView.findViewById<TextInputLayout>(R.id.tilContactPhone)
        val etName = dialogView.findViewById<EditText>(R.id.etContactName)
        val etPhone = dialogView.findViewById<EditText>(R.id.etContactPhone)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSave)

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // Clear error as user types
        etName.addTextChangedListener { tilName.error = null }
        etPhone.addTextChangedListener { tilPhone.error = null }

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val phone = etPhone.text.toString()
            val existing = prefs.getContacts()

            // Run validation check using ContactValidator
            val result = ContactValidator.validateContact(
                name = name,
                phone = phone,
                existingContacts = existing
            )

            if (!result.isValid) {
                tilName.error = result.nameError
                tilPhone.error = result.phoneError
                return@setOnClickListener
            }

            val newContact = Contact(name = name.trim(), phoneNumber = result.normalizedPhone)
            prefs.addContact(newContact)
            loadContacts()
            Toast.makeText(context, "✅ Valid Emergency Contact Saved!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkAndVerifyContact(contact: Contact) {
        val existing = prefs.getContacts()
        val result = ContactValidator.validateContact(
            name = contact.name,
            phone = contact.phoneNumber,
            existingContacts = existing,
            currentContactId = contact.id
        )

        val status = if (result.isValid) "✅ VALID & READY" else "⚠️ INVALID FORMAT"
        val message = if (result.isValid) {
            "Contact Name: ${contact.name}\n" +
            "Phone Number: ${result.normalizedPhone}\n" +
            "Status: Ready to receive emergency SOS alerts."
        } else {
            "Contact Name: ${contact.name}\n" +
            "Phone Number: ${contact.phoneNumber}\n" +
            "Issue: ${result.phoneError ?: result.nameError ?: "Invalid contact details"}"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Contact Check: $status")
            .setMessage(message)
            .setPositiveButton("Send Test SMS") { _, _ ->
                sendTestSms(contact.phoneNumber)
            }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun sendTestSms(number: String) {
        val message = "SMART SOS TEST: You are set as my emergency contact. Reply OK if received."
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$number")
                putExtra("sms_body", message)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not launch SMS app", Toast.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteContact(contact: Contact) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Contact")
            .setMessage("Are you sure you want to remove ${contact.name} from emergency contacts?")
            .setPositiveButton("Delete") { _, _ ->
                prefs.removeContact(contact.id)
                loadContacts()
                Toast.makeText(context, "Contact removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun makePhoneCall(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }
}