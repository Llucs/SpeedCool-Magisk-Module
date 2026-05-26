package com.speedcool.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "speedcool_settings")

class SettingsData(private val context: Context) {

    companion object {
        private val KEY_EXECUTOR_MODE = stringPreferencesKey("executor_mode")
        private val KEY_ACTIVE_PROFILE = stringPreferencesKey("active_profile")
        private val KEY_AUTO_START = booleanPreferencesKey("auto_start")
        private val KEY_BACKGROUND_SERVICE = booleanPreferencesKey("background_service")
        private val KEY_AUTO_RAM_CLEAN = booleanPreferencesKey("auto_ram_clean")
        private val KEY_TEMP_UNIT = stringPreferencesKey("temp_unit")
        private val KEY_RAM_CLEAN_INTERVAL = intPreferencesKey("ram_clean_interval")
        private val KEY_LEARNING_ENABLED = booleanPreferencesKey("learning_enabled")
        private val KEY_CONFLICT_AUTO_RESOLVE = booleanPreferencesKey("conflict_auto_resolve")

        val DEFAULT = Settings(
            executorMode = "shizuku",
            activeProfile = "balanced",
            autoStart = false,
            backgroundService = true,
            autoRamClean = true,
            tempUnit = "celsius",
            ramCleanInterval = 180,
            learningEnabled = true,
            conflictAutoResolve = true
        )
    }

    data class Settings(
        val executorMode: String,
        val activeProfile: String,
        val autoStart: Boolean,
        val backgroundService: Boolean,
        val autoRamClean: Boolean,
        val tempUnit: String,
        val ramCleanInterval: Int,
        val learningEnabled: Boolean,
        val conflictAutoResolve: Boolean
    )

    val settingsFlow: Flow<Settings> = context.dataStore.data.map { prefs ->
        Settings(
            executorMode = prefs[KEY_EXECUTOR_MODE] ?: DEFAULT.executorMode,
            activeProfile = prefs[KEY_ACTIVE_PROFILE] ?: DEFAULT.activeProfile,
            autoStart = prefs[KEY_AUTO_START] ?: DEFAULT.autoStart,
            backgroundService = prefs[KEY_BACKGROUND_SERVICE] ?: DEFAULT.backgroundService,
            autoRamClean = prefs[KEY_AUTO_RAM_CLEAN] ?: DEFAULT.autoRamClean,
            tempUnit = prefs[KEY_TEMP_UNIT] ?: DEFAULT.tempUnit,
            ramCleanInterval = prefs[KEY_RAM_CLEAN_INTERVAL] ?: DEFAULT.ramCleanInterval,
            learningEnabled = prefs[KEY_LEARNING_ENABLED] ?: DEFAULT.learningEnabled,
            conflictAutoResolve = prefs[KEY_CONFLICT_AUTO_RESOLVE] ?: DEFAULT.conflictAutoResolve
        )
    }

    suspend fun updateExecutorMode(mode: String) {
        context.dataStore.edit { it[KEY_EXECUTOR_MODE] = mode }
    }
    suspend fun updateActiveProfile(profile: String) {
        context.dataStore.edit { it[KEY_ACTIVE_PROFILE] = profile }
    }
    suspend fun updateAutoStart(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_START] = enabled }
    }
    suspend fun updateBackgroundService(enabled: Boolean) {
        context.dataStore.edit { it[KEY_BACKGROUND_SERVICE] = enabled }
    }
    suspend fun updateAutoRamClean(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_RAM_CLEAN] = enabled }
    }
    suspend fun updateTempUnit(unit: String) {
        context.dataStore.edit { it[KEY_TEMP_UNIT] = unit }
    }
    suspend fun updateRamCleanInterval(seconds: Int) {
        context.dataStore.edit { it[KEY_RAM_CLEAN_INTERVAL] = seconds }
    }
    suspend fun updateLearningEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_LEARNING_ENABLED] = enabled }
    }
    suspend fun updateConflictAutoResolve(enabled: Boolean) {
        context.dataStore.edit { it[KEY_CONFLICT_AUTO_RESOLVE] = enabled }
    }
}
