package dolphin.android.apps.schoolbell.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "school_bell_settings")

class SettingsRepository(private val context: Context) {

    companion object {
        private val MASTER_SWITCH_KEY = booleanPreferencesKey("master_switch_enabled")
        private val USE_CUSTOM_BELL_KEY = booleanPreferencesKey("use_custom_bell")
        private val IGNORE_BATTERY_WARNING_KEY = booleanPreferencesKey("ignore_battery_optimization_warning")
    }

    val masterSwitchFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Default is true (enabled)
            preferences[MASTER_SWITCH_KEY] ?: true
        }

    val useCustomBellFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Default is true
            preferences[USE_CUSTOM_BELL_KEY] ?: true
        }

    val ignoreBatteryWarningFlow: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            // Default is false
            preferences[IGNORE_BATTERY_WARNING_KEY] ?: false
        }

    suspend fun setMasterSwitch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_SWITCH_KEY] = enabled
        }
    }

    suspend fun setUseCustomBell(useCustom: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[USE_CUSTOM_BELL_KEY] = useCustom
        }
    }

    suspend fun setIgnoreBatteryWarning(ignore: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IGNORE_BATTERY_WARNING_KEY] = ignore
        }
    }
}
