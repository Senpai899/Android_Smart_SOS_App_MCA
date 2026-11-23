package com.example.myapplication.utils

import com.example.myapplication.model.Contact

object ContactValidator {

    data class ValidationResult(
        val isValid: Boolean,
        val nameError: String? = null,
        val phoneError: String? = null,
        val normalizedPhone: String = ""
    )

    private val INDIAN_MOBILE_REGEX = Regex("^(?:\\+?91|0)?[6-9]\\d{9}$")
    private val NAME_REGEX = Regex("^[a-zA-Z\\s.']{2,40}$")

    fun validateContact(
        name: String,
        phone: String,
        existingContacts: List<Contact> = emptyList(),
        currentContactId: String? = null
    ): ValidationResult {
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()

        var nameError: String? = null
        var phoneError: String? = null

        // 1. Name Validation
        if (trimmedName.isEmpty()) {
            nameError = "Contact name cannot be empty"
        } else if (trimmedName.length < 2) {
            nameError = "Name must be at least 2 characters long"
        } else if (!trimmedName.matches(NAME_REGEX)) {
            nameError = "Enter a valid name (letters only)"
        }

        // 2. Clean Phone Number (remove spaces, hyphens, brackets)
        val cleanedPhone = trimmedPhone.replace(Regex("[\\s\\-()]"), "")

        // 3. Indian Phone Number Validation
        var normalizedPhone = cleanedPhone

        if (trimmedPhone.isEmpty()) {
            phoneError = "Phone number cannot be empty"
        } else if (!cleanedPhone.matches(INDIAN_MOBILE_REGEX)) {
            phoneError = "Invalid mobile number! Must be 10 digits starting with 6, 7, 8, or 9 (e.g. 9876543210 or +919876543210)"
        } else {
            // Extract core 10-digit mobile number
            val coreNumber = cleanedPhone.takeLast(10)
            normalizedPhone = "+91 $coreNumber"

            // 4. Duplicate Check
            val isDuplicate = existingContacts.any { contact ->
                val existingCleaned = contact.phoneNumber.replace(Regex("[\\s\\-()]"), "")
                contact.id != currentContactId && (existingCleaned.takeLast(10) == coreNumber)
            }
            if (isDuplicate) {
                phoneError = "This contact phone number is already saved!"
            }
        }

        val isValid = nameError == null && phoneError == null
        return ValidationResult(
            isValid = isValid,
            nameError = nameError,
            phoneError = phoneError,
            normalizedPhone = if (isValid) normalizedPhone else cleanedPhone
        )
    }

    fun isIndianMobileNumberValid(phone: String): Boolean {
        val cleaned = phone.replace(Regex("[\\s\\-()]"), "")
        return cleaned.matches(INDIAN_MOBILE_REGEX)
    }
}