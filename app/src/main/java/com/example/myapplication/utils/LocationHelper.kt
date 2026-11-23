package com.example.myapplication.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.example.myapplication.data.PreferencesManager

object LocationHelper {

    private var cachedLocation: Location? = null

    @SuppressLint("MissingPermission")
    fun getCurrentLocation(context: Context, callback: (Location?) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val prefs = PreferencesManager(context)

        // If in-memory cache is empty, attempt to restore from saved preferences
        if (cachedLocation == null) {
            val savedCoords = prefs.getLastKnownCoordinates()
            if (savedCoords != null) {
                cachedLocation = Location("cached_prefs").apply {
                    latitude = savedCoords.first
                    longitude = savedCoords.second
                    time = System.currentTimeMillis()
                }
            }
        }

        if (locationManager == null) {
            callback(cachedLocation)
            return
        }

        var isGpsEnabled = false
        var isNetworkEnabled = false

        try {
            isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {}

        try {
            isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {}

        // Query all available system providers for fresh fixes
        val systemLocations = mutableListOf<Location>()

        if (isGpsEnabled) {
            try {
                locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { systemLocations.add(it) }
            } catch (_: Exception) {}
        }
        if (isNetworkEnabled) {
            try {
                locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)?.let { systemLocations.add(it) }
            } catch (_: Exception) {}
        }
        try {
            locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)?.let { systemLocations.add(it) }
        } catch (_: Exception) {}

        val freshest = systemLocations.maxByOrNull { it.time }
        if (freshest != null) {
            if (cachedLocation == null || freshest.time >= cachedLocation!!.time) {
                cachedLocation = freshest
                prefs.saveLastLocation(freshest.latitude, freshest.longitude)
            }
        }

        // If we have a cached location from a recent fix or prior alert, return it immediately!
        // This ensures sending distress 2, 3, or more times in a row NEVER drops the location!
        cachedLocation?.let { immediateLocation ->
            callback(immediateLocation)
            // Refresh in background so subsequent alerts have even fresher coordinates if device moved
            refreshLocationInBackground(locationManager, isGpsEnabled, isNetworkEnabled, prefs)
            return
        }

        // If completely first run and no cached location exists, perform a single update with timeout
        val provider = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> LocationManager.PASSIVE_PROVIDER
        }

        val handler = Handler(Looper.getMainLooper())
        var isCompleted = false

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                if (!isCompleted) {
                    isCompleted = true
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: Exception) {}
                    cachedLocation = location
                    prefs.saveLastLocation(location.latitude, location.longitude)
                    callback(location)
                }
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                if (!isCompleted) {
                    isCompleted = true
                    try {
                        locationManager.removeUpdates(this)
                    } catch (_: Exception) {}
                    callback(cachedLocation)
                }
            }
        }

        handler.postDelayed({
            if (!isCompleted) {
                isCompleted = true
                try {
                    locationManager.removeUpdates(listener)
                } catch (_: Exception) {}
                callback(cachedLocation)
            }
        }, 3500L)

        try {
            locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        } catch (e: Exception) {
            e.printStackTrace()
            if (!isCompleted) {
                isCompleted = true
                callback(cachedLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun refreshLocationInBackground(
        locationManager: LocationManager,
        isGpsEnabled: Boolean,
        isNetworkEnabled: Boolean,
        prefs: PreferencesManager
    ) {
        val provider = when {
            isGpsEnabled -> LocationManager.GPS_PROVIDER
            isNetworkEnabled -> LocationManager.NETWORK_PROVIDER
            else -> null
        } ?: return

        val bgListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                try {
                    locationManager.removeUpdates(this)
                } catch (_: Exception) {}
                cachedLocation = location
                prefs.saveLastLocation(location.latitude, location.longitude)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                try {
                    locationManager.removeUpdates(this)
                } catch (_: Exception) {}
            }
        }

        Handler(Looper.getMainLooper()).postDelayed({
            try {
                locationManager.removeUpdates(bgListener)
            } catch (_: Exception) {}
        }, 5000L)

        try {
            locationManager.requestSingleUpdate(provider, bgListener, Looper.getMainLooper())
        } catch (_: Exception) {}
    }
}
