package dolphin.android.apps.schoolbell.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
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
        private val KEY_VOLUME = floatPreferencesKey("bell_volume")
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
            preferences[IGNORE_BATTERY_WARNING_KEY] ?: false
        }

    val volumeFlow: Flow<Float> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val vol = preferences[KEY_VOLUME] ?: 0.5f
            vol.coerceIn(0f, 1f)
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

    suspend fun setVolume(volume: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_VOLUME] = volume.coerceIn(0f, 1f)
        }
    }
}
