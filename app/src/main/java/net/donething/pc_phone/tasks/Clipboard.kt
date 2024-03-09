package net.donething.pc_phone.tasks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.util.Log
import net.donething.pc_phone.MyApp
import net.donething.pc_phone.R
import net.donething.pc_phone.ui.preferences.PreferencesRepository
import net.donething.pc_phone.utils.Http
import org.jetbrains.annotations.Nullable

/**
 * 获取 PC 剪贴板
 */
class ClipboardLoad : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_load_short)

    override fun doTask(): String {
        val pcAddr = PreferencesRepository.taskMode().getServerAddr()
            ?: return MyApp.ctx.getString(R.string.tip_pc_addr_null)

        // 获取 PC 文本/文件
        val obj = Http.getTextOrFile<String>("$pcAddr/api/clip/get")

        Log.i(itag, "响应：'${obj.msg}'")

        if (obj.code != 0) {
            return obj.msg
        }

        val clipManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", obj.msg)
        clipManager.setPrimaryClip(clip)

        return "\"${obj.msg}\""
    }
}

/**
 * 发送剪贴板到 PC
 * Android 10 以后，Activity 必须拥有焦点（前台，而不只是 onResume），才能读取剪贴板
 */
class ClipboardSend(data: String?) : ITask<String>(data) {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_send_short)

    override fun doTask(): String {
        val msg = SendTextToPC(data).doTask()

        Log.i(itag, msg)

        return msg
    }
}

/**
 * 获取 PC 剪贴板
 */
class ClipboardClear : ITask<Nullable>() {
    private val itag = this::class.simpleName

    override val label: String = MyApp.ctx.getString(R.string.shortcut_label_clipboard_clear_short)

    override fun doTask(): String {
        Log.i(itag, "执行：$label")

        val clipManager = MyApp.ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("text", "")
        clipManager.setPrimaryClip(clip)

        return MyApp.ctx.getString(R.string.shortcut_tip_clipboard_clear_success)
    }
}
