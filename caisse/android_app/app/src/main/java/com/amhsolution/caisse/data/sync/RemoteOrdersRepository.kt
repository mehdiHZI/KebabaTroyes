package com.amhsolution.caisse.data.sync

import com.amhsolution.caisse.domain.model.OrderTicket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class RemoteOrdersRepository(
    private val serverConfigRepository: ServerConfigRepository,
    private val api: RemoteOrdersApi = RemoteOrdersApi()
) {
    /**
     * Récupère les commandes ouvertes (Uber / Deliveroo / …) et les fusionne
     * avec la file locale sans écraser les commandes créées en caisse.
     */
    suspend fun pullAndMerge(localOrders: List<OrderTicket>): List<OrderTicket> =
        withContext(Dispatchers.IO) {
            val config = serverConfigRepository.config.first()
            if (!config.isConfigured) return@withContext localOrders

            val remote = api.fetchOpenOrders(config).getOrElse { return@withContext localOrders }
            val localById = localOrders.associateBy { it.id }
            val remoteIds = remote.map { it.orderId }.toSet()

            var nextTicket =
                (localOrders.maxOfOrNull { it.ticketNumber } ?: 1000) + 1

            val remoteTickets = remote.map { order ->
                localById[order.orderId] ?: order.toOrderTicket(nextTicket++)
            }
            val localOnly = localOrders.filter { it.id !in remoteIds }
            (localOnly + remoteTickets).sortedByDescending { it.createdAtEpochMillis }
        }

    suspend fun ackAccepted(orderId: String) = withContext(Dispatchers.IO) {
        val config = serverConfigRepository.config.first()
        api.ackOrder(config, orderId, status = "accepted")
    }

    suspend fun ackCancelled(orderId: String) = withContext(Dispatchers.IO) {
        val config = serverConfigRepository.config.first()
        api.cancelOrder(config, orderId)
    }
}
