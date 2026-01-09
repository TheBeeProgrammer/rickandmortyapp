package com.renato.data.api.interceptor

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Utility class for checking the device's current network connectivity status.
 *
 * This class provides a method to verify whether the device is currently connected
 * to a network that has internet capability.
 *
 * It uses Android's [ConnectivityManager] and [NetworkCapabilities] to perform the check.
 *
 * @constructor Creates an instance with an application context injected by Hilt.
 *
 * @param context The application context used to access the system [ConnectivityManager].
 */
class ConnectionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun isConnected(): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}