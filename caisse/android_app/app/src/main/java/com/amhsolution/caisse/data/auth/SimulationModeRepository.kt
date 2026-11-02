package com.amhsolution.caisse.data.auth

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

private val Context.simulationModeDataStore by preferencesDataStore(name = "caisse_simulation_mode")

/**
 * Mode FACTICE / SIMULATION : tickets séparés des totaux prod et de la sync prod.
 */
class SimulationModeRepository(private val context: Context) {
    private val enabledKey = booleanPreferencesKey("simulation_enabled")

    val isEnabled: Flow<Boolean> =
        context.simulationModeDataStore.data.map { prefs -> prefs[enabledKey] == true }

    suspend fun isEnabledNow(): Boolean = isEnabled.first()

    suspend fun setEnabled(enabled: Boolean) {
        context.simulationModeDataStore.edit { prefs ->
            prefs[enabledKey] = enabled
        }
    }
}
