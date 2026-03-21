package com.aleph.nudge.service

import android.content.Context
import android.util.Log
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v3.base.Reference
import com.clover.sdk.v3.order.LineItem
import com.clover.sdk.v3.order.OrderConnector
import com.clover.sdk.v3.order.OrderV31Connector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OrderObserver(
    private val context: Context,
    private val onNewItem: (orderId: String, itemNames: List<String>) -> Unit
) {

    companion object {
        private const val TAG = "Nudge"
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var orderConnector: OrderConnector? = null
    private var currentOrderId: String? = null
    private val knownLineItemIds = mutableSetOf<String>()

    private val orderListener = object : OrderV31Connector.OnOrderUpdateListener {
        override fun onOrderUpdated(orderId: String?, selfChange: Boolean) {
            if (orderId == null || orderId != currentOrderId) return
            checkForNewItems(orderId)
        }
    }

    fun startObserving(orderId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                currentOrderId = orderId
                synchronized(knownLineItemIds) {
                    knownLineItemIds.clear()
                }

                val account = CloverAccount.getAccount(context) ?: run {
                    Log.w(TAG, "OrderObserver: no Clover account")
                    return@launch
                }
                val connector = OrderConnector(context, account, null)
                connector.connect()
                orderConnector = connector

                val order = connector.getOrder(orderId)
                if (order != null && order.lineItems != null) {
                    synchronized(knownLineItemIds) {
                        for (li in order.lineItems) {
                            if (li.id != null) {
                                knownLineItemIds.add(li.id)
                            }
                        }
                    }
                }

                connector.addOnOrderChangedListener(orderListener)
                Log.d(TAG, "OrderObserver: started observing order $orderId with ${knownLineItemIds.size} existing items")

            } catch (e: Exception) {
                Log.e(TAG, "OrderObserver: failed to start observing", e)
            }
        }
    }

    fun stopObserving() {
        try {
            orderConnector?.removeOnOrderChangedListener(orderListener)
            orderConnector?.disconnect()
            orderConnector = null
            currentOrderId = null
            synchronized(knownLineItemIds) {
                knownLineItemIds.clear()
            }
            scope.cancel()
            Log.d(TAG, "OrderObserver: stopped observing")
        } catch (e: Exception) {
            Log.e(TAG, "OrderObserver: error stopping", e)
        }
    }

    suspend fun addItemToOrder(orderId: String, itemId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val connector = orderConnector
            if (connector == null) {
                Log.w(TAG, "OrderObserver: no connector for addItemToOrder")
                return@withContext false
            }

            val lineItem = LineItem()
            lineItem.item = Reference()
            lineItem.item.id = itemId
            connector.addCustomLineItem(orderId, lineItem, false)
            Log.d(TAG, "OrderObserver: added item $itemId to order $orderId")
            true

        } catch (e: Exception) {
            Log.e(TAG, "OrderObserver: failed to add item to order", e)
            false
        }
    }

    private fun checkForNewItems(orderId: String) {
        scope.launch(Dispatchers.IO) {
            try {
                val connector = orderConnector ?: return@launch
                val order = connector.getOrder(orderId)
                if (order == null || order.lineItems == null) return@launch

                val newItemNames = ArrayList<String>()
                var hasNew = false

                synchronized(knownLineItemIds) {
                    for (li in order.lineItems) {
                        val liId = li.id ?: continue
                        if (!knownLineItemIds.contains(liId)) {
                            knownLineItemIds.add(liId)
                            hasNew = true
                        }
                    }

                    if (hasNew) {
                        for (li in order.lineItems) {
                            newItemNames.add(li.name ?: "Unknown")
                        }
                    }
                }

                if (hasNew && newItemNames.isNotEmpty()) {
                    withContext(Dispatchers.Main) {
                        onNewItem(orderId, newItemNames)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "OrderObserver: error checking for new items", e)
            }
        }
    }
}
