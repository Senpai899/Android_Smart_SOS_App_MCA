package com.example.myapplication.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.myapplication.R
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.service.ShakeSosService
import com.example.myapplication.utils.SosManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch

class SosDashboardFragment : Fragment() {

    private lateinit var prefs: PreferencesManager
    private lateinit var badgeShieldStatus: View
    private lateinit var viewShieldDot: View
    private lateinit var tvShieldStatus: TextView
    private lateinit var cardPermissionWarning: MaterialCardView
    private lateinit var btnGrantPermissions: MaterialButton
    private lateinit var btnSosPanic: View
    private lateinit var switchShakeSos: MaterialSwitch
    private lateinit var cardCustomizeMessage: MaterialCardView
    private lateinit var tvCurrentMessagePreview: TextView
    private lateinit var btnQuickCall112: MaterialCardView
    private lateinit var btnQuickCall100: MaterialCardView

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        if (locationGranted && smsGranted) {
            Toast.makeText(context, "✅ All emergency permissions granted! Automatic SOS is active.", Toast.LENGTH_SHORT).show()
        } else {
            showAndroidPermissionGuideDialog()
        }
        checkAndShowPermissionsWarning()
        updateShakeServiceState()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_sos_dashboard, container, false)
        prefs = PreferencesManager(requireContext())
        initViews(view)
        setupListeners()
        updateShakeServiceState()
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (prefs.isFirstLaunch()) {
            prefs.setFirstLaunchCompleted()
            requestRequiredPermissions()
        }
    }

    override fun onResume() {
        super.onResume()
        checkAndShowPermissionsWarning()
        updateMessagePreview()
        updateShakeServiceState()
    }

    private fun initViews(view: View) {
        badgeShieldStatus = view.findViewById(R.id.badgeShieldStatus)
        viewShieldDot = view.findViewById(R.id.viewShieldDot)
        tvShieldStatus = view.findViewById(R.id.tvShieldStatus)
        cardPermissionWarning = view.findViewById(R.id.cardPermissionWarning)
        btnGrantPermissions = view.findViewById(R.id.btnGrantPermissions)
        btnSosPanic = view.findViewById(R.id.btnSosPanic)
        switchShakeSos = view.findViewById(R.id.switchShakeSos)
        cardCustomizeMessage = view.findViewById(R.id.cardCustomizeMessage)
        tvCurrentMessagePreview = view.findViewById(R.id.tvCurrentMessagePreview)
        btnQuickCall112 = view.findViewById(R.id.btnQuickCall112)
        btnQuickCall100 = view.findViewById(R.id.btnQuickCall100)

        val hasSms = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val hasContacts = prefs.getContacts().isNotEmpty()
        switchShakeSos.isChecked = prefs.isShakeEnabled() && hasSms && hasContacts
    }

    private fun setupListeners() {
        btnGrantPermissions.setOnClickListener {
            handlePermissionRequestClick()
        }

        btnSosPanic.setOnClickListener {
            SosManager.triggerSosAlert(requireContext())
        }

        switchShakeSos.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                val contacts = prefs.getContacts()
                if (contacts.isEmpty()) {
                    switchShakeSos.isChecked = false
                    prefs.setShakeEnabled(false)
                    Toast.makeText(
                        context,
                        "⚠️ Add at least one emergency contact before enabling Shake SOS!",
                        Toast.LENGTH_LONG
                    ).show()
                    updateShakeServiceState()
                    return@setOnCheckedChangeListener
                }

                val hasSms = ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.SEND_SMS
                ) == PackageManager.PERMISSION_GRANTED

                if (!hasSms) {
                    switchShakeSos.isChecked = false
                    prefs.setShakeEnabled(false)
                    Toast.makeText(
                        context,
                        "⚠️ Shake SOS requires SMS permission to send alerts automatically!",
                        Toast.LENGTH_LONG
                    ).show()
                    showAndroidPermissionGuideDialog()
                    updateShakeServiceState()
                    return@setOnCheckedChangeListener
                }
            }

            prefs.setShakeEnabled(isChecked)
            updateShakeServiceState()
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(context, "Shake Emergency Mode $status", Toast.LENGTH_SHORT).show()
        }

        cardCustomizeMessage.setOnClickListener {
            showCustomizeMessageDialog()
        }

        btnQuickCall112.setOnClickListener {
            makePhoneCall("112")
        }

        btnQuickCall100.setOnClickListener {
            makePhoneCall("100")
        }
    }

    private fun updateShakeServiceState() {
        val hasSms = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED
        val hasContacts = prefs.getContacts().isNotEmpty()
        val isShakeEnabled = prefs.isShakeEnabled()

        if (isShakeEnabled && hasSms && hasContacts) {
            ShakeSosService.startService(requireContext())
            if (!switchShakeSos.isChecked) {
                switchShakeSos.isChecked = true
            }
            updateShieldBadge(true)
        } else {
            ShakeSosService.stopService(requireContext())
            if (switchShakeSos.isChecked && (!hasSms || !hasContacts)) {
                switchShakeSos.isChecked = false
                prefs.setShakeEnabled(false)
            }
            val reason = when {
                !hasContacts -> "Shield Inactive: No emergency contacts saved. Add contacts in Contacts tab."
                !hasSms -> "Shield Inactive: SMS permission required to send alerts."
                !isShakeEnabled -> "Shield Inactive: Shake mode is turned off."
                else -> "Shield Inactive"
            }
            updateShieldBadge(false, reason)
        }
    }

    private fun updateShieldBadge(isActive: Boolean, reason: String? = null) {
        if (isActive) {
            badgeShieldStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2534C759"))
            viewShieldDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#34C759"))
            tvShieldStatus.text = "SHIELD ACTIVE"
            tvShieldStatus.setTextColor(Color.parseColor("#34C759"))
        } else {
            badgeShieldStatus.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#25FF3B30"))
            viewShieldDot.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FF3B30"))
            tvShieldStatus.text = "SHIELD INACTIVE"
            tvShieldStatus.setTextColor(Color.parseColor("#FF3B30"))
        }

        badgeShieldStatus.setOnClickListener {
            if (isActive) {
                Toast.makeText(context, "🛡️ Shield Active: Shake detection is on and emergency contacts are ready.", Toast.LENGTH_SHORT).show()
            } else {
                val detail = reason ?: "Shield Inactive: Please add emergency contacts, grant SMS permission, and enable Shake mode."
                Toast.makeText(context, "🛡️ $detail", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkAndShowPermissionsWarning() {
        val hasLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasSms = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasLocation || !hasSms) {
            cardPermissionWarning.visibility = View.VISIBLE
        } else {
            cardPermissionWarning.visibility = View.GONE
        }
    }

    private fun handlePermissionRequestClick() {
        val hasLocation = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasSms = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val showRationaleLocation = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val showRationaleSms = ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            Manifest.permission.SEND_SMS
        )

        if ((!hasLocation && !showRationaleLocation) || (!hasSms && !showRationaleSms)) {
            showAndroidPermissionGuideDialog()
        } else {
            requestRequiredPermissions()
        }
    }

    private fun requestRequiredPermissions() {
        val permissionsToRequest = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.SEND_SMS,
            Manifest.permission.CALL_PHONE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        permissionLauncher.launch(permissionsToRequest.toTypedArray())
    }

    private fun showAndroidPermissionGuideDialog() {
        val title = "🔓 Allow Restricted Settings for SOS"
        val message = "To enable automatic SOS when you shake your phone without manual intervention, Android restricts permissions for directly installed apps:\n\n" +
                "1. Tap 'Open App Info' below.\n" +
                "2. Tap the three dots (⋮) in the top-right corner of App Info.\n" +
                "3. Tap 'Allow restricted settings'.\n" +
                "4. Authenticate with Fingerprint / PIN.\n" +
                "5. Go to 'Permissions' and allow 'SMS' and 'Location'.\n\n" +
                "Once allowed, shaking your phone will automatically send emergency alerts!"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Open App Info") { _, _ ->
                openAppSettings()
            }
            .setNeutralButton("Try Request Again") { _, _ ->
                requestRequiredPermissions()
            }
            .setNegativeButton("Got It", null)
            .show()
    }

    private fun openAppSettings() {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", requireContext().packageName, null)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not open settings automatically", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateMessagePreview() {
        tvCurrentMessagePreview.text = prefs.getSosMessage()
    }

    private fun showCustomizeMessageDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_message, null)
        val etMessage = dialogView.findViewById<EditText>(R.id.etMessageTemplate)
        val btnReset = dialogView.findViewById<MaterialButton>(R.id.btnResetDefault)
        val btnSave = dialogView.findViewById<MaterialButton>(R.id.btnSaveMessage)

        etMessage.setText(prefs.getSosMessage())

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        btnReset.setOnClickListener {
            etMessage.setText(PreferencesManager.DEFAULT_MESSAGE)
        }

        btnSave.setOnClickListener {
            val text = etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                prefs.saveSosMessage(text)
                updateMessagePreview()
                Toast.makeText(context, "SOS Message updated!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Message template cannot be empty", Toast.LENGTH_SHORT).show()
            }
        }

        dialog.show()
    }

    private fun makePhoneCall(number: String) {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
        startActivity(intent)
    }
}