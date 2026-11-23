package com.example.myapplication.data

import android.content.Context
import android.content.SharedPreferences
import com.example.myapplication.model.Contact
import org.json.JSONArray
import org.json.JSONObject

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "safeguard_sos_prefs"
        private const val KEY_CONTACTS = "key_contacts"
        private const val KEY_MESSAGE = "key_sos_message"
        private const val KEY_SHAKE_ENABLED = "key_shake_enabled"
        private const val KEY_FIRST_LAUNCH = "key_first_launch"
        private const val KEY_LAST_LAT = "key_last_lat"
        private const val KEY_LAST_LNG = "key_last_lng"
        
        const val DEFAULT_MESSAGE = "EMERGENCY ALERT! I need immediate help. My current live location is: {LOCATION}"
    }

    fun getContacts(): List<Contact> {
        val jsonString = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        val list = mutableListOf<Contact>()
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Contact(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        name = obj.getString("name"),
                        phoneNumber = obj.getString("phoneNumber")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    fun saveContacts(contacts: List<Contact>) {
        val jsonArray = JSONArray()
        for (contact in contacts) {
            val obj = JSONObject()
            obj.put("id", contact.id)
            obj.put("name", contact.name)
            obj.put("phoneNumber", contact.phoneNumber)
            jsonArray.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, jsonArray.toString()).apply()
    }

    fun addContact(contact: Contact) {
        val current = getContacts().toMutableList()
        current.add(contact)
        saveContacts(current)
    }

    fun removeContact(contactId: String) {
        val current = getContacts().filter { it.id != contactId }
        saveContacts(current)
    }

    fun getSosMessage(): String {
        return prefs.getString(KEY_MESSAGE, DEFAULT_MESSAGE) ?: DEFAULT_MESSAGE
    }

    fun saveSosMessage(message: String) {
        prefs.edit().putString(KEY_MESSAGE, message).apply()
    }

    fun isShakeEnabled(): Boolean {
        return prefs.getBoolean(KEY_SHAKE_ENABLED, true)
    }

    fun setShakeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SHAKE_ENABLED, enabled).apply()
    }

    fun isFirstLaunch(): Boolean {
        return prefs.getBoolean(KEY_FIRST_LAUNCH, true)
    }

    fun setFirstLaunchCompleted() {
        prefs.edit().putBoolean(KEY_FIRST_LAUNCH, false).apply()
    }

    fun saveLastLocation(lat: Double, lng: Double) {
        prefs.edit()
            .putString(KEY_LAST_LAT, lat.toString())
            .putString(KEY_LAST_LNG, lng.toString())
            .apply()
    }

    fun getLastLocationUrl(): String? {
        val lat = prefs.getString(KEY_LAST_LAT, null)
        val lng = prefs.getString(KEY_LAST_LNG, null)
        return if (!lat.isNullOrEmpty() && !lng.isNullOrEmpty()) {
            "https://maps.google.com/?q=$lat,$lng"
        } else {
            null
        }
    }

    fun getLastKnownCoordinates(): Pair<Double, Double>? {
        val lat = prefs.getString(KEY_LAST_LAT, null)?.toDoubleOrNull()
        val lng = prefs.getString(KEY_LAST_LNG, null)?.toDoubleOrNull()
        return if (lat != null && lng != null) Pair(lat, lng) else null
    }
}