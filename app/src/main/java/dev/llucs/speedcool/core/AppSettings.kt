package dev.llucs.speedcool.core

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "speedcool_settings")

object Keys {
    val ACTIVE_PROFILE = stringPreferencesKey("active_profile")
    val ENABLE_ENGINE = booleanPreferencesKey("enable_engine")
    val ENABLE_COOLING = booleanPreferencesKey("enable_cooling")
    val ENABLE_LEARNING = booleanPreferencesKey("enable_learning")
    val COOLING_ON_C = intPreferencesKey("cooling_on_c")
    val COOLING_OFF_C = intPreferencesKey("cooling_off_c")
    val WEEKLY_MASK = intPreferencesKey("weekly_mask")
}

class AppSettings(private val context: Context) {

    val activeProfile: Flow<ProfileId> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[Keys.ACTIVE_PROFILE] ?: ProfileId.BALANCED.name
            runCatching { ProfileId.valueOf(raw) }.getOrElse { ProfileId.BALANCED }
        }

    val enableEngine: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ENABLE_ENGINE] ?: true }

    val enableCooling: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ENABLE_COOLING] ?: true }

    val enableLearning: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.ENABLE_LEARNING] ?: true }

    val coolingOnC: Flow<Int> =
        context.dataStore.data.map { it[Keys.COOLING_ON_C] ?: 45 }

    val coolingOffC: Flow<Int> =
        context.dataStore.data.map { it[Keys.COOLING_OFF_C] ?: 40 }

    val weeklyMask: Flow<Int> =
        context.dataStore.data.map { it[Keys.WEEKLY_MASK] ?: 0 }

    suspend fun setActiveProfile(profile: ProfileId) {
        context.dataStore.edit { it[Keys.ACTIVE_PROFILE] = profile.name }
    }

    suspend fun setEnableEngine(v: Boolean) {
        context.dataStore.edit { it[Keys.ENABLE_ENGINE] = v }
    }

    suspend fun setEnableCooling(v: Boolean) {
        context.dataStore.edit { it[Keys.ENABLE_COOLING] = v }
    }

    suspend fun setEnableLearning(v: Boolean) {
        context.dataStore.edit { it[Keys.ENABLE_LEARNING] = v }
    }

    suspend fun setCoolingThresholds(onC: Int, offC: Int) {
        context.dataStore.edit {
            it[Keys.COOLING_ON_C] = onC
            it[Keys.COOLING_OFF_C] = offC
        }
    }

    suspend fun setWeeklyMask(mask: Int) {
        context.dataStore.edit { it[Keys.WEEKLY_MASK] = mask }
    }
}
