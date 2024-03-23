package net.donething.pc_phone.utils

import android.os.Environment
import com.google.gson.Gson
import net.donething.pc_phone.MyApp
import java.io.File

object Comm {
    // 解析 JSON
    val gson = Gson()

    // 本应用保存文件的根目录
    val fileRoot = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        MyApp.ctx.packageName
    )
}
