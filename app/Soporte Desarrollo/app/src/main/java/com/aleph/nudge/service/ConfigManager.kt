package com.aleph.nudge.service

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class ConfigManager(
    private val context: Context,
    private val backendClient: BackendClient
) {
    private val prefs = context.getSharedPreferences("nudge_config", Context.MODE_PRIVATE)

    data class NudgeConfig(
        val aiModel: String = "glm-4.7-flash",
        val featureFlags: Map<String, Boolean> = emptyMap(),
        val systemPromptOverride: String? = null
    )

    /** Cached config (always available, even offline). */
    fun getCachedConfig(): NudgeConfig {
        return NudgeConfig(
            aiModel = prefs.getString("ai_model", "glm-4.7-flash") ?: "glm-4.7-flash",
            featureFlags = loadFlags(),
            systemPromptOverride = prefs.getString("system_prompt_override", null)
        )
    }

    /** Fetch fresh config from the backend, cache it locally. */
    suspend fun refreshConfig(): NudgeConfig? = withContext(Dispatchers.IO) {
        try {
            val json = backendClient.fetchConfig() ?: return@withContext null
            val config = NudgeConfig(
                aiModel = json.optString("ai_model", "glm-4.7-flash"),
                featureFlags = parseFlags(json.optJSONObject("feature_flags")),
                systemPromptOverride = if (json.has("system_prompt_override") && !json.isNull("system_prompt_override"))
                    json.getString("system_prompt_override")
                else null
            )
            // Cache locally for offline use
            prefs.edit()
                .putString("ai_model", config.aiModel)
                .putString("system_prompt_override", config.systemPromptOverride)
                .putString("feature_flags_json", json.optJSONObject("feature_flags")?.toString() ?: "{}")
                .apply()
            config
        } catch (e: Exception) {
            Log.w("Nudge", "ConfigManager: failed to refresh config", e)
            null
        }
    }

    fun isFeatureEnabled(flag: String): Boolean {
        return getCachedConfig().featureFlags[flag] == true
    }

    private fun parseFlags(json: JSONObject?): Map<String, Boolean> {
        if (json == null) return emptyMap()
        val map = mutableMapOf<String, Boolean>()
        json.keys().forEach { key -> map[key] = json.optBoolean(key, false) }
        return map
    }

    private fun loadFlags(): Map<String, Boolean> {
        val raw = prefs.getString("feature_flags_json", "{}") ?: "{}"
        return parseFlags(JSONObject(raw))
    }
}
