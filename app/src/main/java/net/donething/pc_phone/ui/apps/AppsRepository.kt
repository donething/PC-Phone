package net.donething.pc_phone.ui.apps

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.switchMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.database.AppEntity
import net.donething.pc_phone.database.AppsDao
import java.io.ByteArrayOutputStream
import java.text.Collator
import java.util.Locale

// 应对 kotlin 对中文排序只比较 Unicode 码的问题
val chineseCollator: Collator = Collator.getInstance(Locale.CHINESE)
val comparator = compareBy<AppEntity> { it.installed }.thenBy(chineseCollator, AppEntity::appName)

/**
 * 处理数据库中应用信息的读取、插入
 */
class AppsRepository(private val appsDao: AppsDao) {
    private val itag = this::class.simpleName

    // 数据库中的应用信息：合并备份数据中的应用和已安装的应用，并且标识之
    // 对于从数据库中读取后不需要手动处理的数据，可以简单写为：val allApps = appsDao.getAllApps()
    val allApps: LiveData<List<AppEntity>>
        get() = liveData(Dispatchers.IO) {
            // 首先，创建一个 HashMap，以处理包名和 AppEntity 的映射
            val map = mutableMapOf<String, AppEntity>()

            // 添加本机是否已安装的标识
            getInstalledApps(MyApp.ctx.packageManager).forEach { appEntity ->
                map[appEntity.packageName] = appEntity.apply { installed = true }
            }

            val appsLiveData = appsDao.getAllApps()
            val updatedAppsLiveData = appsLiveData.switchMap { appEntityList ->
                Log.i(itag, "已备份的应用数量为 ${appEntityList.size} 个")
                // 添加已备份的标识
                appEntityList.forEach { appEntity ->
                    val existingAppEntity = map[appEntity.packageName]
                    if (existingAppEntity != null) {
                        //如果 map 中已经存在，设为已备份
                        existingAppEntity.backuped = true
                    } else {
                        //否则，将新的 AppEntity 加入到映射集中，标记其已备份
                        map[appEntity.packageName] = appEntity.apply { backuped = true }
                    }
                }

                val sorted = map.values.sortedWith(comparator)
                liveData { emit(sorted) }
            }

            emitSource(updatedAppsLiveData)
        }

    // 读取本机已安装的用户应用
    private suspend fun getInstalledApps(packageManager: PackageManager): List<AppEntity> {
        return withContext(Dispatchers.IO) {
            val installedApps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            installedApps.filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
                .map { appEntity ->
                    val packageName = appEntity.packageName
                    val appName = packageManager.getApplicationLabel(appEntity).toString()
                    val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "未知"
                    val icon = drawableToByteArray(appEntity.loadIcon(packageManager))

                    AppEntity(packageName, appName, versionName, icon)
                }
        }
    }

    // 插入应用到数据库的新函数
    suspend fun backupAppsIntoDatabase(packageManager: PackageManager) {
        val appEntities = getInstalledApps(packageManager)
        withContext(Dispatchers.IO) {
            appsDao.deleteAll()
            appsDao.insertApps(appEntities)
        }
    }

    // 将图标转为二进制数据，以便保存到数据库
    private fun drawableToByteArray(drawable: Drawable): ByteArray {
        val bitmap = when (drawable) {
            is BitmapDrawable -> {
                drawable.bitmap
            }

            is AdaptiveIconDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            is VectorDrawable -> {
                val bitmap = Bitmap.createBitmap(
                    drawable.intrinsicWidth,
                    drawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
                )
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)
                bitmap
            }

            else -> {
                throw IllegalArgumentException("Unsupported drawable type '${drawable::class}'")
            }
        }

        ByteArrayOutputStream().apply {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, this)
            return toByteArray()
        }
    }
}
