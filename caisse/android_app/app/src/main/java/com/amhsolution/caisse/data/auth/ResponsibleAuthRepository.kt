package com.amhsolution.caisse.data.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amhsolution.caisse.domain.integrity.TicketIntegrity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.security.SecureRandom

private val Context.responsibleAuthDataStore by preferencesDataStore(name = "caisse_responsible_auth")

/**
 * PIN responsable hashé (SHA-256 + sel). Jamais de mot de passe en clair dans le code.
 */
class ResponsibleAuthRepository(private val context: Context) {
    private val pinHashKey = stringPreferencesKey("pin_hash")
    private val pinSaltKey = stringPreferencesKey("pin_salt")

    val isPinConfigured: Flow<Boolean> =
        context.responsibleAuthDataStore.data.map { prefs ->
            !prefs[pinHashKey].isNullOrBlank() && !prefs[pinSaltKey].isNullOrBlank()
        }

    suspend fun isConfigured(): Boolean = isPinConfigured.first()

    suspend fun setPin(pin: String) {
        require(pin.length in 4..12 && pin.all { it.isDigit() }) {
            "Le PIN responsable doit contenir 4 a 12 chiffres."
        }
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
            .joinToString("") { "%02x".format(it) }
        val hash = hashPin(pin, salt)
        context.responsibleAuthDataStore.edit { prefs ->
            prefs[pinSaltKey] = salt
            prefs[pinHashKey] = hash
        }
    }

    /**
     * Vérifie le PIN. Si aucun PIN n'est encore configuré, le premier PIN saisi
     * (4–12 chiffres) est enregistré et considéré comme valide (bootstrap).
     */
    suspend fun verifyOrBootstrap(pin: String): Boolean {
        val prefs = context.responsibleAuthDataStore.data.first()
        val existingHash = prefs[pinHashKey]
        val salt = prefs[pinSaltKey]
        if (existingHash.isNullOrBlank() || salt.isNullOrBlank()) {
            if (pin.length !in 4..12 || !pin.all { it.isDigit() }) return false
            setPin(pin)
            return true
        }
        return hashPin(pin, salt).equals(existingHash, ignoreCase = true)
    }

    private fun hashPin(pin: String, salt: String): String =
        TicketIntegrity.sha256Hex("$salt:$pin")
}
