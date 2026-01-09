package com.renato.data.api.interceptor

import com.renato.domain.model.NetworkUnavailableException
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

/**
 * An OkHttp [Interceptor] that checks for active network connectivity before proceeding with a request.
 *
 * This interceptor prevents network calls when there is no internet connection, throwing
 * a [NetworkUnavailableException] instead. It relies on a [ConnectionManager] implementation
 * to determine network status.
 *
 * This can be especially useful to fail fast and gracefully notify the user of offline scenarios.
 *
 * @param connectionManager A utility that reports current network availability.
 *
 * @throws NetworkUnavailableException when there is no active internet connection.
 */
class NetworkStatusInterceptor @Inject constructor(private val connectionManager: ConnectionManager) :
    Interceptor {
    /**
     * Verifies network connectivity and either proceeds with the HTTP request or fails when offline.
     *
     * @param chain The OkHttp interceptor chain for the outgoing request.
     * @return The HTTP response produced by proceeding with the provided chain.
     * @throws NetworkUnavailableException If there is no active network connection.
     */
    override fun intercept(chain: Interceptor.Chain): Response {
        return if (connectionManager.isConnected()) {
            chain.proceed(chain.request())
        } else {
            throw NetworkUnavailableException()
        }
    }
}