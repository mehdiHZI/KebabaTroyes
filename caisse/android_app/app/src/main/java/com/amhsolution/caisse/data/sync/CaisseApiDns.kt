package com.amhsolution.caisse.data.sync

import okhttp3.Dns
import java.net.InetAddress
import java.net.UnknownHostException

/**
 * DNS système + secours IP publique VPS pour les hôtes Kebaba.
 * Contourne les box Wi‑Fi resto qui ne résolvent pas (Unable to resolve host).
 * Le hostname reste celui de l'URL → certificat TLS / SNI inchangés.
 */
object CaisseApiDns : Dns {
    private val fallbacks: Map<String, String> = mapOf(
        "caisse-api.kebaba.fr" to "141.94.246.122",
        "kebaba.fr" to "141.94.246.122",
        "www.kebaba.fr" to "141.94.246.122"
    )

    override fun lookup(hostname: String): List<InetAddress> {
        return try {
            Dns.SYSTEM.lookup(hostname)
        } catch (first: UnknownHostException) {
            val ip = fallbacks[hostname.lowercase()]
                ?: throw UnknownHostException(
                    "Impossible de résoudre $hostname (DNS Wi‑Fi). " +
                        "Vérifiez l'URL Serveur : https://caisse-api.kebaba.fr"
                ).initCause(first) as UnknownHostException
            try {
                listOf(InetAddress.getByName(ip))
            } catch (second: UnknownHostException) {
                throw UnknownHostException(
                    "DNS HS pour $hostname et secours IP $ip impossible"
                ).initCause(second) as UnknownHostException
            }
        }
    }
}

fun Throwable.toCaisseNetworkMessage(): String {
    val text = (message ?: "").lowercase()
    return when {
        this is UnknownHostException || text.contains("unable to resolve host") ->
            "DNS : hôte introuvable. URL attendue https://caisse-api.kebaba.fr — vérifiez le Wi‑Fi."
        text.contains("timeout") || text.contains("timed out") ->
            "Délai dépassé (réseau lent ou serveur injoignable)."
        text.contains("cleartext") ->
            "HTTP non sécurisé refusé — utilisez https://…"
        else -> message?.take(120) ?: "Erreur réseau"
    }
}
