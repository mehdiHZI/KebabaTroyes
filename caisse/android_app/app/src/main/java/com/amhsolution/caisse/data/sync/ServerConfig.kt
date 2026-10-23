package com.amhsolution.caisse.data.sync

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.amhsolution.caisse.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.serverConfigDataStore by preferencesDataStore(name = "caisse_server_config")

data class ServerConfig(
    val baseUrl: String,
    val apiToken: String,
    val registerId: String
) {
    val isConfigured: Boolean
        get() = baseUrl.isNotBlank() && apiToken.isNotBlank()
}

class ServerConfigRepository(private val context: Context) {
    private val baseUrlKey = stringPreferencesKey("base_url")
    private val apiTokenKey = stringPreferencesKey("api_token")
    private val registerIdKey = stringPreferencesKey("register_id")

    val config: Flow<ServerConfig> =
        context.serverConfigDataStore.data.map { prefs ->
            ServerConfig(
                baseUrl = prefs[baseUrlKey]?.takeIf { it.isNotBlank() }
                    ?: BuildConfig.CAISSE_API_BASE_URL.trim(),
                apiToken = prefs[apiTokenKey]?.takeIf { it.isNotBlank() }
                    ?: BuildConfig.CAISSE_API_TOKEN.trim(),
                registerId = prefs[registerIdKey]?.takeIf { it.isNotBlank() }
                    ?: BuildConfig.CAISSE_REGISTER_ID.trim().ifBlank { "caisse-1" }
            )
        }

    suspend fun update(
        baseUrl: String? = null,
        apiToken: String? = null,
        registerId: String? = null
    ) {
        context.serverConfigDataStore.edit { prefs ->
            baseUrl?.let { prefs[baseUrlKey] = it.trim().trimEnd('/') }
            apiToken?.let { prefs[apiTokenKey] = it.trim() }
            registerId?.let { prefs[registerIdKey] = it.trim() }
        }
    }
}
