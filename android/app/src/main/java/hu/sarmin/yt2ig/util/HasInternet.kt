package hu.sarmin.yt2ig.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkCapabilities.NET_CAPABILITY_VALIDATED
import android.util.Log

fun Context.hasInternet(): Boolean {
    try {
        val connectivityManager = this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return capabilities.hasCapability(NET_CAPABILITY_INTERNET)
                && capabilities.hasCapability(NET_CAPABILITY_VALIDATED)
    } catch (e: Exception) {
        Log.w("Util", "connectivity check failed: ${e.message}", e)
        return true
    }
}
