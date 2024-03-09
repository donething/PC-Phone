package net.donething.pc_phone.ui.preferences

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import net.donething.pc_phone.database.MyDatabase
import net.donething.pc_phone.database.PreferenceEntity

/**
 * 选项的视图模型
 */
class PreferenceViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PreferencesRepository
    val preference: MutableLiveData<PreferenceEntity>

    init {
        val preferenceDao = MyDatabase.getDatabase(application, viewModelScope).preferencesDao()
        repository = PreferencesRepository(preferenceDao, viewModelScope)
        preference = repository.preference
    }

    /**
     * 更新选项到数据库文件。将调用在 IO 线程实际完成
     */
    fun updatePreference(preferenceEntity: PreferenceEntity) {
        viewModelScope.launch {
            repository.updatePreference(preferenceEntity)
        }
    }

    /**
     * 分享数据库文件以备份。将调用在 IO 线程实际完成
     */
    fun backupDB(context: Context) {
        viewModelScope.launch {
            repository.backupDB(context)
        }
    }
}
