package com.aleph.nudge.service

import android.content.Context
import android.util.Log
import com.clover.sdk.util.CloverAccount
import com.clover.sdk.v1.customer.CustomerConnector
import com.clover.sdk.v3.order.OrderConnector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomerDataService(private val context: Context) {

    companion object {
        private const val TAG = "Nudge"
    }

    data class CustomerContext(
        val customerName: String?,
        val orderHistory: List<String>, // past item names
        val visitCount: Int
    )

    /**
     * Try to get customer info associated with the current order.
     * Returns null if no customer is linked or customer data is unavailable.
     */
    suspend fun getCustomerForOrder(orderId: String): CustomerContext? = withContext(Dispatchers.IO) {
        var orderConnector: OrderConnector? = null
        var customerConnector: CustomerConnector? = null
        try {
            val account = CloverAccount.getAccount(context) ?: run {
                return@withContext null
            }

            orderConnector = OrderConnector(context, account, null)
            orderConnector.connect()

            val order = orderConnector.getOrder(orderId)
            val customerId = order?.customers?.firstOrNull()?.id

            if (customerId == null) {
                Log.d(TAG, "CustomerDataService: no customer linked to order $orderId")
                return@withContext null
            }

            customerConnector = CustomerConnector(context, account, null)
            customerConnector.connect()

            val customer = customerConnector.getCustomer(customerId)
            if (customer == null) {
                Log.d(TAG, "CustomerDataService: customer $customerId not found")
                return@withContext null
            }

            val customerName = listOfNotNull(customer.firstName, customer.lastName)
                .joinToString(" ")
                .ifBlank { null }

            // Get customer's order history for frequently ordered items
            val orders = customer.orders
            val pastItemNames = mutableListOf<String>()
            if (orders != null) {
                // Take up to 10 most recent orders
                var consecutiveFailures = 0
                for (orderRef in orders.take(10)) {
                    try {
                        val pastOrder = orderConnector.getOrder(orderRef.id)
                        pastOrder?.lineItems?.forEach { li ->
                            val name = li.name
                            if (name != null) pastItemNames.add(name)
                        }
                        consecutiveFailures = 0
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to fetch order history: ${e.javaClass.simpleName}")
                        consecutiveFailures++
                        if (consecutiveFailures >= 2) {
                            Log.w(TAG, "Multiple consecutive failures, stopping history fetch")
                            break
                        }
                    }
                }
            }

            val ctx = CustomerContext(
                customerName = customerName,
                orderHistory = pastItemNames,
                visitCount = orders?.size ?: 0
            )

            Log.d(TAG, "CustomerDataService: found customer (id=${customerId}) with ${pastItemNames.size} past items, ${ctx.visitCount} visits")
            ctx

        } catch (e: Exception) {
            Log.w(TAG, "CustomerDataService: failed to get customer data", e)
            null
        } finally {
            try { orderConnector?.disconnect() } catch (_: Exception) {}
            try { customerConnector?.disconnect() } catch (_: Exception) {}
        }
    }

    /**
     * Build a prompt-friendly summary from customer context.
     */
    fun buildCustomerPromptContext(ctx: CustomerContext): String {
        val lines = mutableListOf<String>()

        if (ctx.customerName != null) {
            lines.add("Customer: ${ctx.customerName} (${ctx.visitCount} previous visits)")
        } else {
            lines.add("Returning customer (${ctx.visitCount} previous visits)")
        }

        if (ctx.orderHistory.isNotEmpty()) {
            // Count item frequency
            val freq = ctx.orderHistory.groupingBy { it }.eachCount()
                .entries.sortedByDescending { it.value }
                .take(8)

            val freqLines = freq.joinToString(", ") { "${it.key} (${it.value}x)" }
            lines.add("Their frequently ordered items: $freqLines")
        }

        return lines.joinToString("\n")
    }
}
