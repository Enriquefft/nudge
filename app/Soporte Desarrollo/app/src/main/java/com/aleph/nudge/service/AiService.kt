package com.aleph.nudge.service

import android.util.Log
import com.aleph.nudge.BuildConfig
import com.aleph.nudge.model.MenuItem
import com.aleph.nudge.model.Suggestion
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

class AiService {

    companion object {
        private const val TAG = "Nudge"
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    /** Set by NudgeApplication after initialization. */
    var backendClient: BackendClient? = null

    private var systemPrompt: String = ""
    private val knownItems = ConcurrentHashMap<String, MenuItem>()

    fun setMenuContext(items: List<MenuItem>) {
        knownItems.clear()
        for (item in items) {
            knownItems[item.id] = item
        }

        val regularItems = items.filter { !it.isModifier }
        val modifiers = items.filter { it.isModifier }

        val itemLines = regularItems.joinToString("\n") { item ->
            val cat = if (item.category != null) " [${item.category}]" else ""
            "- ${item.id}: ${item.name} ${item.priceFormatted}$cat"
        }

        val modifierLines = if (modifiers.isNotEmpty()) {
            modifiers.joinToString("\n") { mod ->
                val group = if (mod.modifierGroupName != null) " (${mod.modifierGroupName})" else ""
                "- ${mod.id}: ${mod.name} ${mod.priceFormatted}$group"
            }
        } else {
            "None"
        }

        systemPrompt = """You are Nudge, an upselling assistant for a POS system.

This merchant's menu:
$itemLines

Available modifiers:
$modifierLines"""

        Log.d(TAG, "AiService: menu context set with ${items.size} items")
    }

    suspend fun getSuggestion(
        currentItems: List<String>,
        upsellHistory: String? = null,
        customerContext: String? = null
    ): Suggestion? = withContext(Dispatchers.IO) {
        if (systemPrompt.isEmpty()) {
            Log.w(TAG, "AiService: no menu context set")
            return@withContext null
        }

        try {
            val orderList = currentItems.joinToString(", ")

            val contextSections = mutableListOf<String>()
            contextSections.add("Current order: $orderList")

            if (upsellHistory != null) {
                contextSections.add(upsellHistory)
            }

            if (customerContext != null) {
                contextSections.add(customerContext)
            }

            val userPrompt = """${contextSections.joinToString("\n\n")}

Suggest ONE complementary item or modifier from this merchant's menu.

Return ONLY valid JSON:
{ "item_id": "id", "item_name": "name", "price": "X.XX", "reason": "short phrase staff says to customer" }

Rules:
- Must be an item/modifier from the menu above (use exact item_id)
- Must NOT be already in the order
- Prefer add-ons and modifiers > sides > drinks > desserts > mains
- Reason must be conversational, specific, under 10 words
- Example reason: "Most people add fries with a burger"
- If upsell history is provided, AVOID items with low acceptance rates and FAVOR items that customers frequently accept
- If customer data is provided, personalize the suggestion to their preferences"""

            val messagesArray = com.google.gson.JsonArray()

            val sysMsg = JsonObject()
            sysMsg.addProperty("role", "system")
            sysMsg.addProperty("content", systemPrompt)
            messagesArray.add(sysMsg)

            val userMsg = JsonObject()
            userMsg.addProperty("role", "user")
            userMsg.addProperty("content", userPrompt)
            messagesArray.add(userMsg)

            val requestBody = JsonObject()
            requestBody.addProperty("model", BuildConfig.ZAI_MODEL)
            requestBody.addProperty("max_tokens", 256)
            requestBody.addProperty("temperature", 0.7)
            requestBody.add("messages", messagesArray)

            val requestBodyJson = gson.toJson(requestBody)

            // Try backend proxy first (centralizes API key management).
            val backendResponse = backendClient?.proxySuggest(requestBodyJson)
            if (backendResponse != null) {
                Log.d(TAG, "AiService: got response via backend proxy")
                val suggestion = parseResponse(backendResponse)
                if (suggestion == null) {
                    Log.w(TAG, "AiService: parseResponse returned null (backend proxy)")
                }
                return@withContext suggestion
            }

            // Fallback: direct call to Z.ai
            Log.d(TAG, "AiService: backend proxy unavailable, falling back to direct Z.ai")
            val body = requestBodyJson.toRequestBody(JSON_MEDIA_TYPE)

            val request = Request.Builder()
                .url("${BuildConfig.ZAI_BASE_URL}/chat/completions")
                .addHeader("Authorization", "Bearer ${BuildConfig.ZAI_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                Log.e(TAG, "AiService: API error code=${response.code} body=$responseBody")
                return@withContext null
            }

            Log.d(TAG, "AiService: API response received (direct), parsing...")
            val suggestion = parseResponse(responseBody)
            if (suggestion == null) {
                Log.w(TAG, "AiService: parseResponse returned null")
            }
            suggestion

        } catch (e: Exception) {
            Log.e(TAG, "AiService: request failed: ${e.message}", e)
            null
        }
    }

    private fun parseResponse(responseBody: String): Suggestion? {
        try {
            val root = JsonParser.parseString(responseBody).asJsonObject
            val choices = root.getAsJsonArray("choices")
            if (choices == null || choices.size() == 0) {
                Log.w(TAG, "AiService: no choices in response")
                return null
            }

            val message = choices.get(0).asJsonObject.getAsJsonObject("message")
            val content = message.get("content").asString.trim()

            val jsonStart = content.indexOf('{')
            val jsonEnd = content.lastIndexOf('}')
            if (jsonStart < 0 || jsonEnd < 0 || jsonEnd <= jsonStart) {
                Log.w(TAG, "AiService: no JSON found in content: $content")
                return null
            }

            val jsonStr = content.substring(jsonStart, jsonEnd + 1)
            val suggestionJson = JsonParser.parseString(jsonStr).asJsonObject

            val itemId = suggestionJson.get("item_id").asString
            val itemName = suggestionJson.get("item_name").asString
            val priceStr = suggestionJson.get("price").asString
            val reason = suggestionJson.get("reason").asString

            if (!knownItems.containsKey(itemId)) {
                Log.w(TAG, "AiService: suggested item_id '$itemId' not in menu, trying name match")
                val matchByName = knownItems.values.find { it.name.equals(itemName, ignoreCase = true) }
                if (matchByName != null) {
                    return Suggestion(
                        itemId = matchByName.id,
                        itemName = matchByName.name,
                        price = matchByName.price,
                        reason = reason
                    )
                }
                Log.w(TAG, "AiService: no match for '$itemName' either, discarding suggestion")
                return null
            }

            val priceCents = parsePriceToCents(priceStr)

            return Suggestion(
                itemId = itemId,
                itemName = itemName,
                price = priceCents,
                reason = reason
            )
        } catch (e: Exception) {
            Log.e(TAG, "AiService: failed to parse response", e)
            return null
        }
    }

    private fun parsePriceToCents(priceStr: String): Long {
        val cleaned = priceStr.replace("$", "").trim()
        val parts = cleaned.split(".")
        val dollars = parts[0].toLongOrNull() ?: 0L
        val cents = if (parts.size > 1) {
            val cStr = parts[1].take(2).padEnd(2, '0')
            cStr.toLongOrNull() ?: 0L
        } else {
            0L
        }
        return dollars * 100 + cents
    }
}
