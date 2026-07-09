package dolphin.android.apps.SchoolBell.data

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

    suspend fun setMasterSwitch(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[MASTER_SWITCH_KEY] = enabled
        }
    }
}
