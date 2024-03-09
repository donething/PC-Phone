package net.donething.pc_phone.ui.apps

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.database.AppEntity
import net.donething.pc_phone.database.MyDatabase

class AppsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: AppsRepository
    val allApps: LiveData<List<AppEntity>>

    init {
        val appDao = MyDatabase.getDatabase(application, viewModelScope).appsDao()
        repository = AppsRepository(appDao)
        allApps = repository.allApps
    }

    /**
     * 备份应用信息到数据库，等完成后显示提示
     */
    fun backupAppsIntoDatabase() {
        viewModelScope.launch {
            repository.backupAppsIntoDatabase(getApplication<Application>().packageManager)

            // 在主线程上显示 Toast 信息
            withContext(Dispatchers.Main) {
                Toast.makeText(MyApp.ctx, "已完成备份用户应用的信息", Toast.LENGTH_LONG).show()
            }
        }
    }
}