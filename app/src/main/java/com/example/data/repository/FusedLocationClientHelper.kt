package com.example.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

object FusedLocationClientHelper {

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onLocationFound: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationFound(location.latitude, location.longitude)
                } else {
                    // Fallback to last known location or system LocationManager
                    fallbackLocationManager(context, onLocationFound, onError)
                }
            }.addOnFailureListener {
                fallbackLocationManager(context, onLocationFound, onError)
            }
        } catch (e: SecurityException) {
            onError("Location permission not granted: ${e.localizedMessage}")
        } catch (e: Exception) {
            fallbackLocationManager(context, onLocationFound, onError)
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLocationManager(
        context: Context,
        onLocationFound: (latitude: Double, longitude: Double) -> Unit,
        onError: (String) -> Unit
    ) {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            if (locationManager == null) {
                onError("Location manager unavailable")
                return
            }

            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null

            for (provider in providers) {
                val loc = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || loc.accuracy < bestLocation.accuracy) {
                    bestLocation = loc
                }
            }

            if (bestLocation != null) {
                onLocationFound(bestLocation.latitude, bestLocation.longitude)
            } else {
                onError("Unable to pinpoint exact GPS coordinates. Using default city preset.")
            }
        } catch (e: Exception) {
            onError("GPS Error: ${e.localizedMessage}")
        }
    }
}
