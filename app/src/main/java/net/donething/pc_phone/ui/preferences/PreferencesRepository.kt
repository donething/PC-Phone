package net.donething.pc_phone.ui.preferences

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.database.MyDatabase
import net.donething.pc_phone.database.PreferenceEntity
import net.donething.pc_phone.database.PreferencesDao
import java.io.File

/**
 * 处理数据库中选项的读取、插入
 */
class PreferencesRepository(private val preferencesDao: PreferencesDao, private val scope: CoroutineScope) {
    companion object {
        /**
         * 数据库中保存的选项，没有时新建为默认选项。仅在 IO 线程上使用，快捷使用
         *
         * 用函数而不是属性：该行需要运行在 IO 线程。因为作为变量时，谁先调用 PreferencesRepository 初始化，
         * 就在谁的线程上完成该变量的初始化。而函数只是声明不会运行里面的语句
         */
        fun taskMode() = MyDatabase.getDatabase(MyApp.ctx, CoroutineScope(Dispatchers.IO))
            .preferencesDao().getPreference() ?: PreferenceEntity()
    }

    /**
     * 数据库中保存的选项。没有时新建为默认选项
     */
    val preference = MutableLiveData<PreferenceEntity>()

    init {
        loadPreference()
    }

    /**
     * 加载选项到MutableLiveData。将在 IO 线程中完成
     */
    private fun loadPreference() {
        scope.launch(Dispatchers.IO) {
            val pref = preferencesDao.getPreference()
            preference.postValue(pref ?: PreferenceEntity())
        }
    }

    /**
     * 更新选项到数据库。将在 IO 线程中完成
     */
    suspend fun updatePreference(preferenceEntity: PreferenceEntity) {
        withContext(Dispatchers.IO) {
            preferencesDao.insertOrUpdatePreference(preferenceEntity)
        }
    }

    /**
     * 分享数据库文件以备份。将在 IO 线程完毕文件操作
     */
    suspend fun backupDB(context: Context) {
        var outputFile: File
        withContext(Dispatchers.IO) {
            outputFile = MyDatabase.backupDatabase(context)
        }

        // 现在您可以用 FileProvider 分享 outputFile
        val fileUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", outputFile)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, fileUri)
            type = "application/octet-stream"
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        context.startActivity(Intent.createChooser(shareIntent, "保存备份数据到..."))
    }
}
