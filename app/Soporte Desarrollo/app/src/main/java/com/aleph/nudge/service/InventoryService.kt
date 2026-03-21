package com.aleph.nudge.service

import android.content.Context
import android.util.Log
import com.aleph.nudge.model.MenuItem
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v3.inventory.InventoryConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

class InventoryService(private val context: Context) {

    companion object {
        private const val TAG = "Nudge"
    }

    private val itemCache = ConcurrentHashMap<String, MenuItem>()

    suspend fun loadInventory(): List<MenuItem> = withContext(Dispatchers.IO) {
        var connector: InventoryConnector? = null
        try {
            val account = CloverAccount.getAccount(context) ?: run {
                Log.w(TAG, "InventoryService: no Clover account")
                return@withContext emptyList()
            }
            connector = InventoryConnector(context, account, null)
            connector.connect()

            val cloverItems = connector.items ?: emptyList()
            val cloverCategories = connector.categories ?: emptyList()
            val cloverModifierGroups = connector.modifierGroups ?: emptyList()

            val categoryMap = HashMap<String, String>()
            for (category in cloverCategories) {
                val catItems = category.items
                if (catItems != null) {
                    for (ref in catItems) {
                        categoryMap[ref.id] = category.name ?: ""
                    }
                }
            }

            val allMenuItems = ArrayList<MenuItem>()

            for (item in cloverItems) {
                val menuItem = MenuItem(
                    id = item.id ?: continue,
                    name = item.name ?: "Unknown",
                    price = item.price ?: 0L,
                    category = categoryMap[item.id],
                    isModifier = false,
                    modifierGroupName = null
                )
                allMenuItems.add(menuItem)
            }

            for (group in cloverModifierGroups) {
                val groupName = group.name ?: "Modifiers"
                val modifiers = group.modifiers
                if (modifiers != null) {
                    for (modifier in modifiers) {
                        val menuItem = MenuItem(
                            id = modifier.id ?: continue,
                            name = modifier.name ?: "Unknown",
                            price = modifier.price ?: 0L,
                            category = null,
                            isModifier = true,
                            modifierGroupName = groupName
                        )
                        allMenuItems.add(menuItem)
                    }
                }
            }

            itemCache.clear()
            for (item in allMenuItems) {
                itemCache[item.id] = item
            }

            Log.d(TAG, "InventoryService: loaded ${allMenuItems.size} items (${cloverItems.size} items, ${cloverModifierGroups.size} modifier groups)")

            allMenuItems

        } catch (e: Exception) {
            Log.e(TAG, "InventoryService: failed to load inventory", e)
            emptyList()
        } finally {
            try {
                connector?.disconnect()
            } catch (e: Exception) {
                Log.w(TAG, "InventoryService: error disconnecting", e)
            }
        }
    }

    fun getMenuItems(): List<MenuItem> {
        return ArrayList(itemCache.values)
    }

    fun getItemById(id: String): MenuItem? {
        return itemCache[id]
    }
}
