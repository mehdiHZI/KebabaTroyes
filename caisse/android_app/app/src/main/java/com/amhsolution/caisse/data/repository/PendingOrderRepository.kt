package com.amhsolution.caisse.data.repository

import com.amhsolution.caisse.data.local.toJsonPendingOrders
import com.amhsolution.caisse.data.local.toPendingOrders
import com.amhsolution.caisse.domain.model.OrderTicket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class PendingOrderRepository(
    private val file: File
) {
    suspend fun loadPendingOrders(): List<OrderTicket> = withContext(Dispatchers.IO) {
        if (!file.exists()) return@withContext emptyList()
        runCatching { file.readText().toPendingOrders() }.getOrDefault(emptyList())
    }

    suspend fun savePendingOrders(orders: List<OrderTicket>) = withContext(Dispatchers.IO) {
        file.parentFile?.mkdirs()
        file.writeText(orders.toJsonPendingOrders())
    }
}
