package net.donething.pc_phone.ui.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

/**
 * 自定义 DataStore，用于选项页面
 */
class CustomDataStore(private val dataStore: DataStore<Preferences>) : PreferenceDataStore() {
    override fun putString(key: String, value: String?) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[stringPreferencesKey(key)] = value ?: ""
            }
        }
    }

    override fun getString(key: String, defValue: String?): String? {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[stringPreferencesKey(key)] ?: defValue
            }.first()
        }
    }

    override fun putInt(key: String, value: Int) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[intPreferencesKey(key)] = value
            }
        }
    }

    override fun getInt(key: String, defValue: Int): Int {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[intPreferencesKey(key)] ?: defValue
            }.first()
        }
    }

    override fun putLong(key: String, value: Long) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[longPreferencesKey(key)] = value
            }
        }
    }

    override fun getLong(key: String, defValue: Long): Long {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[longPreferencesKey(key)] ?: defValue
            }.first()
        }
    }

    override fun putFloat(key: String, value: Float) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[floatPreferencesKey(key)] = value
            }
        }
    }

    override fun getFloat(key: String, defValue: Float): Float {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[floatPreferencesKey(key)] ?: defValue
            }.first()
        }
    }

    override fun putBoolean(key: String, value: Boolean) {
        runBlocking {
            dataStore.edit { preferences ->
                preferences[booleanPreferencesKey(key)] = value
            }
        }
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean {
        return runBlocking {
            dataStore.data.map { preferences ->
                preferences[booleanPreferencesKey(key)] ?: defValue
            }.first()
        }
    }
}