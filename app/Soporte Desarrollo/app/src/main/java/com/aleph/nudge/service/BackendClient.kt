package com.aleph.nudge.service

import android.content.Context
import android.util.Log
import com.aleph.nudge.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class BackendClient(private val context: Context) {

    companion object {
        private const val TAG = "BackendClient"
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    private val baseUrl = BuildConfig.BACKEND_URL
    private val prefs = context.getSharedPreferences("nudge_backend", Context.MODE_PRIVATE)

    /** Stored after registration. */
    var apiToken: String?
        get() = prefs.getString("api_token", null)
        private set(value) = prefs.edit().putString("api_token", value).apply()

    var merchantId: String?
        get() = prefs.getString("merchant_id", null)
        private set(value) = prefs.edit().putString("merchant_id", value).apply()

    val isRegistered: Boolean get() = apiToken != null

    /**
     * Register this device with the backend.
     * Returns true if registration succeeded.
     */
    fun register(merchantName: String, deviceId: String): Boolean {
        val json = JSONObject().apply {
            put("device_id", deviceId)
            put("merchant_name", merchantName)
            put("app_version", BuildConfig.VERSION_NAME)
            put("platform", "android")
        }

        val request = Request.Builder()
            .url("$baseUrl/api/register")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val body = JSONObject(response.body?.string() ?: "")
                apiToken = body.getString("api_token")
                merchantId = body.getString("merchant_id")
                true
            } else {
                Log.w(TAG, "Registration returned ${response.code}")
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Registration failed", e)
            false
        }
    }

    /**
     * Proxy an AI suggestion request through the backend.
     * Returns the raw response body on success, or null on failure.
     */
    fun proxySuggest(requestBody: String): String? {
        val token = apiToken ?: return null

        val request = Request.Builder()
            .url("$baseUrl/api/suggest")
            .header("Authorization", "Bearer $token")
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                response.body?.string()
            } else {
                Log.w(TAG, "Suggest returned ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Backend suggest failed, will fallback", e)
            null
        }
    }

    /**
     * Send analytics events to the backend.
     */
    fun sendEvents(events: List<Map<String, Any>>): Boolean {
        val token = apiToken ?: return false

        val json = JSONObject().apply {
            put("events", JSONArray(events.map { JSONObject(it) }))
        }

        val request = Request.Builder()
            .url("$baseUrl/api/events")
            .header("Authorization", "Bearer $token")
            .post(json.toString().toRequestBody("application/json".toMediaType()))
            .build()

        return try {
            client.newCall(request).execute().isSuccessful
        } catch (e: Exception) {
            Log.w(TAG, "Event send failed", e)
            false
        }
    }

    /**
     * Fetch remote configuration.
     */
    fun fetchConfig(): JSONObject? {
        val token = apiToken ?: return null

        val request = Request.Builder()
            .url("$baseUrl/api/config")
            .header("Authorization", "Bearer $token")
            .get()
            .build()

        return try {
            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                JSONObject(response.body?.string() ?: "{}")
            } else {
                Log.w(TAG, "Config fetch returned ${response.code}")
                null
            }
        } catch (e: Exception) {
            Log.w(TAG, "Config fetch failed", e)
            null
        }
    }
}
