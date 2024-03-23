package net.donething.pc_phone.entity

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// 文件信息
data class FileInfo(
    val path: String,   // 文件的完整路径
    val name: String,   // 文件名
    val isDir: Boolean,
    val size: Long,
    val modTime: Long    // 时间戳（毫秒）
)

fun FileInfo.toFormattedString(): String {
    return "FileInfo(name=${this.name}, isDir=${this.isDir}, size=${this.size}, modTime=${
        SimpleDateFormat("yyyy-MM-dd", Locale.CHINA).format(Date(this.modTime))
    })"
}
