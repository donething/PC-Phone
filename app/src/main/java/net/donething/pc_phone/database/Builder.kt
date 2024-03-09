package net.donething.pc_phone.database

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.CoroutineScope
import net.donething.pc_phone.MyApp
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.InputStream

/**
 * 数据库名
 */
val DB_NAME = "${MyApp.ctx.packageName}.db"

/**
 * 数据库
 */
@Database(entities = [AppEntity::class, PreferenceEntity::class], version = 1, exportSchema = false)
abstract class MyDatabase : RoomDatabase() {
    /**
     * 各个数据表的 DAO
     */
    abstract fun appsDao(): AppsDao
    abstract fun preferencesDao(): PreferencesDao

    companion object {
        private val itag = MyDatabase::class.simpleName

        @Volatile
        private var INSTANCE: MyDatabase? = null

        /**
         * 创建/获取数据库的实例
         */
        fun getDatabase(context: Context, scope: CoroutineScope): MyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(context.applicationContext, MyDatabase::class.java, DB_NAME)
                    .setJournalMode(JournalMode.TRUNCATE)
                    .fallbackToDestructiveMigration()
                    .addCallback(MyDatabaseCallback(scope))
                    .build()
                INSTANCE = instance

                // return instance
                instance
            }
        }

        /**
         * 备份数据库文件
         */
        fun backupDatabase(context: Context): File {
            val dbFile = context.getDatabasePath(DB_NAME)
            val cacheDir = context.cacheDir
            val outputFile = File(cacheDir, dbFile.name)

            if (!dbFile.exists()) {
                throw FileNotFoundException("Database file not found")
            }

            // 复制数据库到临时目录，以便分享发送
            dbFile.inputStream().use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }

            Log.i(itag, "backupDatabase: 已复制数据文件到'${outputFile.absolutePath}'")

            return outputFile
        }

        /**
         * 恢复数据库文件
         */
        fun restoreDatabase(context: Context, inputStream: InputStream) {
            // 将输入流的数据写入文件输出流
            val outputFile = context.getDatabasePath(DB_NAME)
            val fileOutputStream = FileOutputStream(outputFile)
            inputStream.use { input ->
                fileOutputStream.use { output ->
                    input.copyTo(output)
                }
            }

            Log.i(itag, "restoreDatabase: 已恢复数据文件到'${outputFile.absolutePath}'")
        }

        private class MyDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            // Override the onOpen method to populate the database.
            // Additional callback methods exist and can be overridden as needed.
        }
    }
}
