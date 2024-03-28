package net.donething.pc_phone.database

import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query

/**
 * 选项页面的表
 */
@Entity(tableName = "preferences")
data class PreferenceEntity(
    // 作为设置使用，目前只会有唯一行，使用固定的 ID 1
    @PrimaryKey val id: Int = 1,
    @ColumnInfo(defaultValue = "") var securityAuth: String = "",
    var pcLanIP: String = "",
    var pcServerPort: String = "",
    var pcMAC: String = ""
) {
    /**
     * 返回 PC 端的服务地址
     *
     * 当选项中的 PC 局域网地址为空时，返回空 null
     */
    fun getServerAddr(): String? {
        if (pcLanIP.isBlank() || pcServerPort.isBlank()) {
            return null
        }

        return "http://$pcLanIP:$pcServerPort"
    }
}

/**
 * 选项页面的 DAO
 */
@Dao
interface PreferencesDao {
    @Query("SELECT * FROM preferences WHERE id = 1")
    fun getPreference(): PreferenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdatePreference(preference: PreferenceEntity)
}
