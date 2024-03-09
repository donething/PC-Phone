package net.donething.pc_phone.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * 备份的用户应用的信息的表
 */
@Entity(tableName = "apps")
data class AppEntity(
    @PrimaryKey val packageName: String,
    val appName: String,
    val versionName: String,
    val appIcon: ByteArray,
) {
    // 不存进数据库的属性
    /**
     * 显示时本机是否已安装该包的应用
     */
    @Ignore
    var installed: Boolean = false

    /**
     * 是否已备份过
     */
    @Ignore
    var backuped: Boolean = false
}

/**
 * 备份的用户应用的信息的表的 DAO
 */
@Dao
interface AppsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppEntity>)

    @Query("SELECT * FROM apps")
    fun getAllApps(): LiveData<List<AppEntity>>

    @Query("DELETE FROM apps")
    fun deleteAll()
}

