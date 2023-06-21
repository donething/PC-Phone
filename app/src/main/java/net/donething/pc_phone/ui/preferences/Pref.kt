package net.donething.pc_phone.ui.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

/**
 * Preference DataStore 实例。继承了 Context 的组件可用，如 `Myapp.ctx`
 */
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "preferences")

/**
 * Preference 选项页面中的 keys
 */
object Pref {
    const val PC_ADDR = "pc_addr"
    const val PC_MAC = "pc_mac"
}
